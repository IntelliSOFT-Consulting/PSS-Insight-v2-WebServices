package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.EnvConfig;
import com.intellisoft.pssnationalinstance.db.AboutUs;
import com.intellisoft.pssnationalinstance.db.Benchmarks;
import com.intellisoft.pssnationalinstance.db.IndicatorEdits;
import com.intellisoft.pssnationalinstance.db.VersionEntity;
import com.intellisoft.pssnationalinstance.repository.AboutUsRepository;
import com.intellisoft.pssnationalinstance.repository.BenchmarksRepository;
import com.intellisoft.pssnationalinstance.repository.VersionEntityRepository;
import com.intellisoft.pssnationalinstance.service_impl.service.AboutUsService;
import com.intellisoft.pssnationalinstance.service_impl.service.IndicatorEditsService;
import com.intellisoft.pssnationalinstance.service_impl.service.InternationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.NationalTemplateService;
import com.intellisoft.pssnationalinstance.util.EnvUrlConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class NationalTemplateServiceImpl implements NationalTemplateService {

    private final FormatterClass formatterClass = new FormatterClass();

    private final InternationalTemplateService internationalTemplateService;
    private final VersionEntityRepository versionEntityRepository;
    private final IndicatorEditsService indicatorEditsService;
    private final AboutUsService aboutUsService;
    private final AboutUsRepository aboutUsRepository;
    private final BenchmarksRepository benchmarksRepository;
    private final EnvUrlConstants envUrlConstants;
    private final EnvConfig envConfig;

    private HttpEntity<String> getHeaders() {

        String username = envConfig.getValue().getUsername();
        String password = envConfig.getValue().getPassword();

        String auth = username + ":" + password;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64Utils.encodeToString(auth.getBytes()));

        return new HttpEntity<>(headers);

    }

    private List<IndicatorBenchmark> getIndicatorCodesFromBenchmarksAPI() {
        String url = envUrlConstants.getFETCH_BENCHMARKS_API();
        Flux<IndicatorBenchmark> responseFlux = WebClient.builder().baseUrl(url).defaultHeaders(headers -> headers.addAll(getHeaders().getHeaders())).build().get().retrieve().bodyToFlux(IndicatorBenchmark.class);

        List<IndicatorBenchmark> benchmarkList = responseFlux.collectList().block();

        if (benchmarkList != null) {
            List<IndicatorBenchmark> indicatorBenchmarks = new ArrayList<>();
            for (IndicatorBenchmark benchmarks : benchmarkList) {
                String Benchmark = (String) benchmarks.getBenchmark();
                String Indicator_Code = (String) benchmarks.getIndicatorCode();
                indicatorBenchmarks.add(benchmarks);
            }
            return indicatorBenchmarks;
        }
        return Collections.emptyList();
    }

    @Override
    public Results getNationalPublishedVersion() {

        try {

            DbPublishedVersionDetails publishedVersionValues = getPublishedDetails();
            List<DbIndicators> details = publishedVersionValues.getDetails();

            // Merge DbIndicators with the same categoryName. Use LinkedHashMap
            Map<Object, DbIndicators> mergedMap = new LinkedHashMap<>();
            for (DbIndicators dbIndicators : details) {
                Object categoryName = dbIndicators.getCategoryName();
                if (!mergedMap.containsKey(categoryName)) {
                    mergedMap.put(categoryName, dbIndicators);
                } else {
                    DbIndicators existingDbIndicators = mergedMap.get(categoryName);
                    List<DbIndicatorValues> mergedIndicatorValues = new ArrayList<>(existingDbIndicators.getIndicators());
                    mergedIndicatorValues.addAll(dbIndicators.getIndicators());
                    mergedMap.put(categoryName, new DbIndicators(categoryName, mergedIndicatorValues));
                }
            }
            // Resulting list after merging
            List<DbIndicators> mergedList = new ArrayList<>(mergedMap.values());
            publishedVersionValues.setDetails(mergedList);

            return new Results(200, publishedVersionValues);

        } catch (Exception syntaxException) {
            log.error("An error occurred while fetching national indicators");
        }
        return new Results(400, "The national indicators could not be found.");

    }

    @Override
    public Results getNationalDetails() {
        try {

            DbPublishedVersion publishedVersionValues = nationalPublishedIndicators();
            if (publishedVersionValues != null) {

                int no = 0;
                List<DbIndicatorsApp> dbIndicatorsAppList = new ArrayList<>();
                List<DbIndicators> dbIndicatorsList = publishedVersionValues.getDetails();
                for (DbIndicators dbIndicators : dbIndicatorsList) {
                    String name = (String) dbIndicators.getCategoryName();

                    List<DbIndicatorValuesApp> dbIndicatorValuesAppList = new ArrayList<>();
                    List<DbIndicatorValues> indicatorValuesList = dbIndicators.getIndicators();
                    no = no + indicatorValuesList.size();
                    for (DbIndicatorValues dbIndicatorValues : indicatorValuesList) {
                        String categoryName = (String) dbIndicatorValues.getCategoryName();
                        String description = (String) dbIndicatorValues.getDescription();
                        String categoryId = (String) dbIndicatorValues.getCategoryId();
                        String indicatorName = (String) dbIndicatorValues.getIndicatorName();
                        List<DbIndicatorDataValues> dataValuesList = dbIndicatorValues.getIndicatorDataValue();
                        DbIndicatorValuesApp dbIndicatorValuesApp = new DbIndicatorValuesApp(name, description, categoryId, categoryName, indicatorName, dataValuesList);


                        dbIndicatorValuesAppList.add(dbIndicatorValuesApp);
                    }

                    DbIndicatorsApp dbIndicatorsApp = new DbIndicatorsApp(name, dbIndicatorValuesAppList);
                    dbIndicatorsAppList.add(dbIndicatorsApp);

                }

                DbPublishedVersionApp dbPublishedVersionApp = new DbPublishedVersionApp(no, dbIndicatorsAppList);

                List<AboutUs> aboutUsList = aboutUsService.getAboutUs(true, 10, 1);
                DbMobileData data = new DbMobileData(dbPublishedVersionApp, aboutUsList);
                return new Results(200, data);
            }

        } catch (Exception syntaxException) {
            log.error("An error occurred while fetching national indicators");
        }
        return new Results(400, "The national indicators could not be found.");

    }

    public DbPublishedVersion getThePreviousIndicators(String versionNumber) {
        String publishedBaseUrl = envUrlConstants.getINTERNATIONAL_PUBLISHED_VERSIONS() + versionNumber;
        DbMetadataJson dbMetadataJson = internationalTemplateService.getIndicators(publishedBaseUrl);
        if (dbMetadataJson != null) {
            DbPrograms dbPrograms = dbMetadataJson.getMetadata();
            if (dbPrograms != null) {
                return dbPrograms.getPublishedVersion();
            }
        }
        return null;
    }

    public DbPublishedVersion nationalPublishedIndicators() {
        String publishedBaseUrl = envUrlConstants.getNATIONAL_PUBLISHED_VERSIONS();
        DbMetadataJson dbMetadataJson = internationalTemplateService.getPublishedData(publishedBaseUrl);
        if (dbMetadataJson != null) {
            DbPrograms dbPrograms = dbMetadataJson.getMetadata();
            if (dbPrograms != null) {
                return dbPrograms.getPublishedVersion();
            }
        }
        return null;
    }

    private DbPublishedVersionDetails getPublishedDetails() {
        DbPublishedVersionDetails details = new DbPublishedVersionDetails(null, null, null, null, Collections.emptyList());
        String publishedBaseUrl = envUrlConstants.getNATIONAL_PUBLISHED_VERSIONS();
        String dataDictionaryUrl = envUrlConstants.getINDICATOR_DESCRIPTIONS();

        //Get metadata json
        Flux<DbIndicatorDetails> responseFlux = WebClient.builder().baseUrl(dataDictionaryUrl).defaultHeaders(headers -> headers.addAll(getHeaders().getHeaders())).build().get().retrieve().bodyToFlux(DbIndicatorDetails.class);


        List<DbIndicatorDetails> responseList = responseFlux.collectList().block();

        DbMetadataJson dbMetadataJson = internationalTemplateService.getPublishedData(publishedBaseUrl);
        if (dbMetadataJson != null) {
            DbPrograms dbPrograms = dbMetadataJson.getMetadata();
            if (dbMetadataJson.getMetadata() != null) {
                String referenceSheet = (String) dbMetadataJson.getMetadata().getReferenceSheet();
                details.setReferenceSheet(referenceSheet);
            }
            if (dbPrograms != null) {
                DbPublishedVersion dbPublishedVersion = dbPrograms.getPublishedVersion();

                List<AboutUs> publishedDetails = aboutUsRepository.findAll();
                if (!publishedDetails.isEmpty()) {
                    for (AboutUs aboutUs : publishedDetails) {
                        details.setAboutUs(aboutUs.getAboutUs());
                        details.setContactUs(aboutUs.getContactUs());

                        if (dbPublishedVersion != null) {
                            details.setDetails(dbPublishedVersion.getDetails());
                        }
                        if (dbPublishedVersion != null) {
                            details.setCount(dbPublishedVersion.getCount());
                        }
                    }
                }
            }

            List<IndicatorBenchmark> indicatorCodes = getIndicatorCodesFromBenchmarksAPI();

            for (DbIndicatorDetails indicatorDetails : responseList) {
                for (DbAssessmentQuestion dbAssessmentQuestion : indicatorDetails.getAssessmentQuestions()) {
                    String assessmentQuestionName = (String) dbAssessmentQuestion.getName();

                    for (DbIndicators dbIndicators : details.getDetails()) {
                        for (DbIndicatorValues dbIndicatorValues : dbIndicators.getIndicators()) {
                            List<DbIndicatorDataValues> indicatorDataValues = dbIndicatorValues.getIndicatorDataValue();

                            // Find the matching DbIndicatorDataValues object
                            Optional<DbIndicatorDataValues> matchingDataValue = indicatorDataValues.stream().filter(dataValue -> assessmentQuestionName.equals(dataValue.getName())).findFirst();

                            if (matchingDataValue.isPresent()) {
                                // Set the id, code, and value type
                                DbIndicatorDataValues dataValue = matchingDataValue.get();
                                dataValue.setId(dbAssessmentQuestion.getId());
                                dataValue.setCode(dbAssessmentQuestion.getCode());
                                dataValue.setValueType(dbAssessmentQuestion.getValueType());

                                String categoryName = (String) dbIndicatorValues.getCategoryName();
                                String categoryCode = (String) dataValue.getCode();

                                boolean stringsMatch = categoryName.equals(categoryCode);
                                if (stringsMatch) {
                                    Optional<Benchmarks> optionalBenchmarks = benchmarksRepository.findFirstByIndicatorCode(categoryCode);
                                    if (optionalBenchmarks.isPresent()) {
                                        Benchmarks benchmarks = optionalBenchmarks.get();
                                        String nationalValue = benchmarks.getNationalValue();
                                        String internationalValue = benchmarks.getValue();
                                        dbIndicatorValues.setBenchmark(nationalValue);
                                        dbIndicatorValues.setInternationalBenchmark(internationalValue);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return details;
    }

    @Override
    public Results getIndicatorDescription(String pssCode) {

        String description = "Indicator Description";
        try {
            String publishedBaseUrlNational = envUrlConstants.getNATIONAL_PUBLISHED_VERSIONS();
            DbMetadataJsonNational dbMetadataJsonNational = getMetadataNational(publishedBaseUrlNational);

            if (dbMetadataJsonNational != null) {
                DbProgramsDataDetails dbPrograms = dbMetadataJsonNational.getMetadata();
                if (dbPrograms != null) {
                    List<DbIndicatorDescriptionData> dbIndicatorDescriptionList = dbPrograms.getIndicatorDescriptions();
                    description = getIndicatorDescriptionNational(pssCode, dbIndicatorDescriptionList);
                }

            } else {
                //Check from the international indicator description
                String publishedBaseUrlInternational = envUrlConstants.getINTERNATIONAL_PUBLISHED_VERSIONS();
                DbMetadataJson dbMetadataJsonInternational = getMetadata(publishedBaseUrlInternational);
                if (dbMetadataJsonInternational != null) {
                    DbPrograms dbPrograms = dbMetadataJsonInternational.getMetadata();
                    if (dbPrograms != null) {
                        List<DbIndicatorDescriptionInt> indicatorDescriptionNational = (List<DbIndicatorDescriptionInt>) dbPrograms.getIndicatorDescriptions();
                        description = getIndicatorDescriptionInterNational(pssCode, indicatorDescriptionNational);
                    }
                }
            }

        } catch (Exception e) {
            log.error("An error occurred while fetching indicator description");
            return new Results(400, "Indicator could not be found.");
        }

        return new Results(200, new DbDetails(description));
    }

    private String getIndicatorDescriptionNational(String code, List<DbIndicatorDescriptionData> dbIndicatorDescriptionList) {

        String description = "";
        for (DbIndicatorDescriptionData indicatorDescription : dbIndicatorDescriptionList) {
            if (code.equals(indicatorDescription.getIndicator_Code())) {
                description = indicatorDescription.getDescription();
                break;
            }
        }
        return description;
    }

    private String getIndicatorDescriptionInterNational(String code, List<DbIndicatorDescriptionInt> dbIndicatorDescriptionList) {

        String description = "";
        for (DbIndicatorDescriptionInt indicatorDescription : dbIndicatorDescriptionList) {
            if (code.equals(indicatorDescription.getIndicator_Code())) {
                description = indicatorDescription.getDescription();
                break;
            }
        }
        return description;
    }

    private DbMetadataJson getMetadata(String url) {
        return internationalTemplateService.getPublishedData(url);
    }

    private DbMetadataJsonNational getMetadataNational(String url) {
        return internationalTemplateService.getPublishedDataNational(url);
    }

    @Async
    public void savePublishedVersion(String createdBy, String versionId, List<DbVersionDate> indicatorList) {
        try {

            String nationalPublishedUrl = envUrlConstants.getNATIONAL_PUBLISHED_VERSIONS();
            String interNationalPublishedUrl = envUrlConstants.getINTERNATIONAL_PUBLISHED_VERSIONS();

            String versionNumberLatest = "1";
            String versionNumberPast = "1";

            int nationalLatestVersion = 1;
            try {
                int versionNo = internationalTemplateService.getVersions(interNationalPublishedUrl);
                nationalLatestVersion = internationalTemplateService.getVersions(nationalPublishedUrl);

                versionNumberLatest = String.valueOf(versionNo + 1);

                if (versionNo > 1) {
                    versionNumberPast = String.valueOf(versionNo - 1);
                }

            } catch (Exception e) {
                log.error("An error occurred while saving published versions");
            }

            List<DbIndicators> indicatorsList = new ArrayList<>();

            List<String> latestIndicators = new ArrayList<>();
            List<String> pastIndicators = new ArrayList<>();

            for (DbVersionDate dbVersionDate : indicatorList) {

                boolean isLatest = dbVersionDate.isLatest();
                String id = dbVersionDate.getId();
                if (isLatest) {
                    latestIndicators.add(id);
                } else {
                    pastIndicators.add(id);
                }
            }

            /**
             * Get past metadata from the version number ** Previous
             */
            DbPublishedVersion pastInternationalIndicators = getThePreviousIndicators(versionNumberPast);

            if (pastInternationalIndicators != null) {

                List<DbIndicators> indicatorValuesList = getSelectedIndicators(pastInternationalIndicators.getDetails(), pastIndicators, versionNumberPast);
                if (!indicatorValuesList.isEmpty()) {
                    indicatorsList.addAll(indicatorValuesList);
                }
            }


            /**
             * Get international Latest metadata
             */
            String internationalPublishedUrl = envUrlConstants.getINTERNATIONAL_PUBLISHED_VERSIONS();
            DbMetadataJson dbMetadataJson = getMetadata(internationalPublishedUrl);
            String versionNo = String.valueOf(nationalLatestVersion + 1);

            int vers = Integer.parseInt(versionNumberPast);
            int currentVersionNumber = vers + 1;

            if (dbMetadataJson != null) {

                DbPrograms dbPrograms = dbMetadataJson.getMetadata();
                if (dbPrograms != null) {
                    DbPublishedVersion publishedVersionValues = dbPrograms.getPublishedVersion();
                    if (publishedVersionValues != null) {
                        List<DbIndicators> indicatorValuesList = getSelectedIndicators(publishedVersionValues.getDetails(), latestIndicators, String.valueOf(currentVersionNumber));
                        if (!indicatorValuesList.isEmpty()) {
                            indicatorsList.addAll(indicatorValuesList);
                        }

                    }

                }


                //Get updates from db and include them
                List<IndicatorEdits> indicatorEditsList = indicatorEditsService.getIndicatorEditsCreator(createdBy);
                for (IndicatorEdits indicatorEdits : indicatorEditsList) {

                    String categoryId = indicatorEdits.getCategoryId();
                    String indicatorId = indicatorEdits.getIndicatorId();
                    String edits = indicatorEdits.getEdit();

                    for (DbIndicators dbIndicators : indicatorsList) {
                        List<DbIndicatorValues> indicators = dbIndicators.getIndicators();
                        for (DbIndicatorValues dbIndicatorValues : indicators) {
                            String dbCategoryId = (String) dbIndicatorValues.getCategoryId();
                            List<DbIndicatorDataValues> indicatorDataValueList = dbIndicatorValues.getIndicatorDataValue();
                            if (categoryId.equals(dbCategoryId)) {
                                if (categoryId.equals(indicatorId)) {
                                    dbIndicatorValues.setIndicatorName(edits);
                                } else {
                                    for (DbIndicatorDataValues dbIndicatorDataValues : indicatorDataValueList) {
                                        String dbIndicatorId = (String) dbIndicatorDataValues.getId();
                                        if (indicatorId.equals(dbIndicatorId)) {
                                            dbIndicatorDataValues.setName(edits);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                indicatorEditsService.deleteEditByCategoryId(createdBy);

                //Set new values
                DbPublishedVersion dbPublishedVersion = new DbPublishedVersion(indicatorsList.size(), indicatorsList);

                assert dbPrograms != null;
                dbPrograms.setPublishedVersion(dbPublishedVersion);
                dbMetadataJson.setMetadata(dbPrograms);

                dbMetadataJson.setVersion(versionNo);

            }

            String authHeader = "Basic " + Base64.getEncoder().encodeToString((envConfig.getValue().getUsername() + ":" + envConfig.getValue().getPassword()).getBytes());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.add("Authorization", authHeader);

            var response = GenericWebclient.postForSingleObjResponseWithAuth(nationalPublishedUrl + versionNo, dbMetadataJson, DbMetadataJson.class, DbPublishVersionResponse.class, authHeader);

            if (response.getHttpStatusCode() == 201) {
                Optional<VersionEntity> optionalVersionEntity = versionEntityRepository.findById(Long.valueOf(versionId));
                if (optionalVersionEntity.isPresent()) {

                    VersionEntity versionEntity = optionalVersionEntity.get();
                    versionEntity.setVersionName(versionNo);
                    versionEntityRepository.save(versionEntity);
                }

            }

        } catch (Exception e) {
            log.error("An error occurred while saving published version");
        }
    }

    public List<DbIndicators> getSelectedIndicators(List<DbIndicators> details, List<String> selectedIndicators, String versionNumber) {
        List<DbIndicators> dbIndicatorsList = new ArrayList<>();
        for (DbIndicators dbIndicators : details) {
            String categoryName = (String) dbIndicators.getCategoryName();

            List<DbIndicatorValues> newIndicators = new ArrayList<>();

            List<DbIndicatorValues> indicatorValuesList = dbIndicators.getIndicators();
            for (DbIndicatorValues indicatorValues : indicatorValuesList) {

                if (indicatorValues.getCategoryId() != null) {
                    String categoryId = String.valueOf(indicatorValues.getCategoryId());
                    if (selectedIndicators.contains(categoryId)) {
                        indicatorValues.setVersionNumber(versionNumber);
                        newIndicators.add(indicatorValues);
                    }
                }

            }
            DbIndicators dbNewIndicators = new DbIndicators(categoryName, newIndicators);
            dbIndicatorsList.add(dbNewIndicators);
        }
        return dbIndicatorsList;
    }


    @Override
    public DbMetadataJson getPublishedMetadataJson() {
        String publishedBaseUrl = envUrlConstants.getNATIONAL_PUBLISHED_VERSIONS();
        return internationalTemplateService.getPublishedData(publishedBaseUrl);
    }

    @Override
    public Results getOrgUnits(int pageNo) {

        try {
            DbOrganisationUnit dbOrganisationUnit = WebClient.builder().baseUrl(envUrlConstants.getNATIONAL_BASE_ORG_UNIT() + pageNo).defaultHeaders(headers -> headers.addAll(getHeaders().getHeaders())).exchangeStrategies(ExchangeStrategies.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build()).build().get().retrieve().bodyToMono(DbOrganisationUnit.class).block();


            if (dbOrganisationUnit != null) {

                List<DbProgramsValue> dbProgramsValueList = dbOrganisationUnit.getOrganisationUnits();
                DbResults dbResults = new DbResults(dbProgramsValueList.size(), dbProgramsValueList);
                return new Results(200, dbResults);

            }

        } catch (Exception e) {
            log.error("An error occurred while fetching Organisation units");
        }


        return new Results(400, "There was an issue processing the request");
    }

    @Override
    public int getCurrentVersion(String url) {

        try {
            var response = WebClient.builder().baseUrl(url).defaultHeaders(headers -> headers.addAll(getHeaders().getHeaders())).build().get().retrieve().bodyToMono(List.class).block();
            if (!response.isEmpty()) {
                return formatterClass.getNextVersion(response);
            } else {
                return 1;
            }
        } catch (Exception e) {
            return 1;
        }
    }


}
