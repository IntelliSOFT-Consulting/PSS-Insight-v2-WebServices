package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.EnvConfig;
import com.intellisoft.pssnationalinstance.db.DataEntry;
import com.intellisoft.pssnationalinstance.db.DataEntryResponses;
import com.intellisoft.pssnationalinstance.db.PeriodConfiguration;
import com.intellisoft.pssnationalinstance.db.Surveys;
import com.intellisoft.pssnationalinstance.repository.DataEntryRepository;
import com.intellisoft.pssnationalinstance.repository.DataEntryResponsesRepository;
import com.intellisoft.pssnationalinstance.repository.SurveysRepo;
import com.intellisoft.pssnationalinstance.service_impl.service.*;
import com.intellisoft.pssnationalinstance.util.EnvUrlConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import javax.transaction.Transactional;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class DataEntryServiceImpl implements DataEntryService {


    private final FormatterClass formatterClass = new FormatterClass();
    private final DataEntryRepository dataEntryRepository;
    private final DataEntryResponsesRepository dataEntryResponsesRepository;
    private final NationalTemplateService nationalTemplateService;
    private final PeriodConfigurationService periodConfigurationService;
    private final InternationalTemplateService internationalTemplateService;
    private final SurveysRepo surveysRepo;
    private final JavaMailSenderService javaMailSenderService;
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


    @Override
    public Results addDataEntry(DbDataEntryData dbDataEntryData) {

        String selectedPeriod = dbDataEntryData.getSelectedPeriod();
        boolean isPublished = dbDataEntryData.isPublished();
        String dataEntryPersonId = dbDataEntryData.getDataEntryPersonId();
        String dateEntryDate = dbDataEntryData.getDataEntryDate();
        String orgUnit = dbDataEntryData.getOrgUnit();
        String username = dbDataEntryData.getDataEntryPerson().getUsername();
        String firstName = dbDataEntryData.getDataEntryPerson().getFirstName();
        String surname = dbDataEntryData.getDataEntryPerson().getSurname();
        String email = dbDataEntryData.getDataEntryPerson().getEmail();

        String status = PublishStatus.DRAFT.name();
        if (isPublished) {
            status = PublishStatus.PUBLISHED.name();
        }

        String versionNo = getCurrentVersion();
        DataEntry dataEntry = new DataEntry();
        dataEntry.setStatus(status);
        dataEntry.setSelectedPeriod(selectedPeriod);
        dataEntry.setDataEntryPersonId(dataEntryPersonId);
        dataEntry.setDataEntryDate(dateEntryDate);
        dataEntry.setVersionNumber(versionNo);
        dataEntry.setOrgUnit(orgUnit);
        dataEntry.setEmail(email);
        dataEntry.setUsername(username);
        dataEntry.setFirstName(firstName);
        dataEntry.setSurname(surname);
        DataEntry dataEntryAdded = dataEntryRepository.save(dataEntry);


        List<DbDataEntryResponses> responsesList = dbDataEntryData.getResponses();
        for (DbDataEntryResponses dbDataEntryResponses : responsesList) {
            String indicatorId = dbDataEntryResponses.getIndicator();
            String response = dbDataEntryResponses.getResponse();
            String comment = dbDataEntryResponses.getComment();
            String attachment = dbDataEntryResponses.getAttachment();

            DataEntryResponses dataEntryResponses = new DataEntryResponses();
            dataEntryResponses.setResponse(response);
            dataEntryResponses.setComment(comment);
            dataEntryResponses.setAttachment(attachment);
            dataEntryResponses.setIndicator(indicatorId);
            dataEntryResponses.setDataEntry(dataEntryAdded);

            dataEntryResponsesRepository.save(dataEntryResponses);
        }

        if (isPublished) {

            saveEventData(dbDataEntryData);

        }


        return new Results(201, new DbDetails("Data submitted successfully."));
    }

    private String getCurrentVersion() {
        int versionNumber = nationalTemplateService.getCurrentVersion(envUrlConstants.getNATIONAL_PUBLISHED_VERSIONS());
        return String.valueOf(versionNumber);
    }

    @Async
    public void saveEventData(DbDataEntryData dbDataEntryData) {


        String selectedPeriod = dbDataEntryData.getSelectedPeriod();
        String dataEntryPersonId = dbDataEntryData.getDataEntryPersonId();
        String orgUnit = dbDataEntryData.getOrgUnit();
        List<DbDataValues> dbDataValuesList = new ArrayList<>();

        List<DbDataEntryResponses> responsesList = dbDataEntryData.getResponses();
        for (DbDataEntryResponses dbDataEntryResponses : responsesList) {

            String indicatorId = dbDataEntryResponses.getIndicator();
            String dbResponse = dbDataEntryResponses.getResponse();
            String comment = dbDataEntryResponses.getComment();
            String attachment = dbDataEntryResponses.getAttachment();

            String response = "";
            if (dbResponse != null) {
                if (dbResponse.equals("Yes")) {
                    response = "true";
                } else if (dbResponse.equals("No")) {
                    response = "false";
                } else {
                    response = dbResponse;
                }
            }


            //Get the saved national template, and check for the ids and get code and add comments and uploads
            DbMetadataJson dbMetadataJson = nationalTemplateService.getPublishedMetadataJson();
            if (dbMetadataJson != null) {
                DbPrograms metadata = dbMetadataJson.getMetadata();
                if (metadata != null) {
                    DbGroups groups = metadata.getGroups();
                    if (groups != null) {
                        List<DbDataElementGroups> dataElementGroups = groups.getDataElementGroups();
                        for (DbDataElementGroups dbDataElementGroups : dataElementGroups) {
                            List<DbDataElements> dataElementsList = dbDataElementGroups.getDataElements();
                            for (DbDataElements dataElements : dataElementsList) {

                                String id = dataElements.getId();
                                String code = dataElements.getCode();

                                if (indicatorId.equals(id)) {
                                    //Add _Comments and _Uploads
                                    String codeComments = code + "_Comments";
                                    String uploadComments = code + "_Uploads";
                                    String commentId = getCommentsUploads(codeComments, dataElementsList);
                                    String uploadId = getCommentsUploads(uploadComments, dataElementsList);

                                    if (comment != null && !commentId.equals("")) {
                                        DbDataValues dbDataValues = new DbDataValues(commentId, comment);
                                        dbDataValuesList.add(dbDataValues);
                                    }
                                    if (attachment != null && !uploadId.equals("")) {
                                        DbDataValues dbDataValues = new DbDataValues(uploadId, attachment);
//                                        dbDataValuesList.add(dbDataValues);
                                    }


                                }

                            }
                        }

                    }
                }
            }

            DbDataValues dbDataValues = new DbDataValues(indicatorId, response);
            dbDataValuesList.add(dbDataValues);

        }

        if (selectedPeriod != null) {
            PeriodConfiguration periodConfiguration = periodConfigurationService.getConfigurationDetails(selectedPeriod);
            String status = DhisStatus.ACTIVE.name();

            if (periodConfiguration != null) {
                boolean isCompleted = periodConfiguration.isCompleted();
                if (isCompleted) {
                    status = PublishStatus.COMPLETED.name();
                }
            }

            try {
                DbProgramsData dbProgramsData = WebClient.builder().baseUrl(envUrlConstants.getNATIONAL_BASE_PROGRAMS()).defaultHeaders(headers -> headers.addAll(getHeaders().getHeaders())).exchangeStrategies(ExchangeStrategies.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build()).build().get().retrieve().bodyToMono(DbProgramsData.class).block();


                if (dbProgramsData != null) {
                    String id = "";
                    List<DbProgramsValue> programsValueList = dbProgramsData.getPrograms();
                    for (int i = 0; i < programsValueList.size(); i++) {
                        id = programsValueList.get(i).getId().toString();
                    }
                    DbDataEntry dataEntry = new DbDataEntry(id, orgUnit, selectedPeriod + "-01-" + "01", status, dataEntryPersonId, dbDataValuesList);

                    ObjectMapper objectMapper = new ObjectMapper();
                    String json = objectMapper.writeValueAsString(dataEntry);

                    String authHeader = "Basic " + Base64.getEncoder().encodeToString((envConfig.getValue().getUsername() + ":" + envConfig.getValue().getPassword()).getBytes());
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                    headers.add("Authorization", authHeader);

                    DbEvents response = GenericWebclient.postForSingleObjResponseWithAuth(envUrlConstants.getEVENTS_ENDPOINT(), dataEntry, DbDataEntry.class, DbEvents.class, authHeader);

                    if (response.getHttpStatusCode() == 200) {
                        String surveyId = dbDataEntryData.getSurveyId();
                        if (surveyId != null) {
                            updateSurveyDetails(surveyId);
                        }

                    }
                }


            } catch (Exception e) {
                log.error("An error occurred during event data processing");
            }


        }

    }

    public void updateSurveyDetails(String surveyId) {

        Optional<Surveys> optionalSurveys = surveysRepo.findById(Long.valueOf(surveyId));
        if (optionalSurveys.isPresent()) {
            Surveys surveys = optionalSurveys.get();
            surveys.setStatus(SurveySubmissionStatus.VERIFIED.name());
            surveysRepo.save(surveys);
        }

    }

    @Override
    public Results viewDataEntry(String id) throws URISyntaxException {

        Optional<DataEntry> optionalDataEntry = dataEntryRepository.findById(Long.valueOf(id));
        if (optionalDataEntry.isPresent()) {

            DataEntry dataEntry = optionalDataEntry.get();
            List<DataEntryResponses> dataEntryResponseList = dataEntryResponsesRepository.findByDataEntry(dataEntry);


            String publishedBaseUrl = envUrlConstants.getNATIONAL_PUBLISHED_VERSIONS();
            DbMetadataJson dbMetadataJson = internationalTemplateService.getPublishedData(publishedBaseUrl);

            if (dbMetadataJson != null) {
                DbPrograms dbPrograms = dbMetadataJson.getMetadata();
                if (dbMetadataJson.getMetadata() != null) {
                    String referenceSheet = (String) dbMetadataJson.getMetadata().getReferenceSheet();

                    DbDataEntryResponse dbDataEntryResponse = new DbDataEntryResponse(dataEntry.getId(), dataEntry.getSelectedPeriod(), dataEntry.getStatus(), dataEntry.getDataEntryPersonId(), dataEntry.getDataEntryDate(), dataEntry.getCreatedAt(), dataEntryResponseList, referenceSheet, null);


                    String versionNumber = dataEntry.getVersionNumber();
                    if (versionNumber != null) {

                        String indicatorDescriptionUrl = envUrlConstants.getINDICATOR_DESCRIPTIONS();
                        String indicatorDescription = WebClient.builder().baseUrl(indicatorDescriptionUrl).defaultHeaders(headers -> headers.addAll(getHeaders().getHeaders())).exchangeStrategies(ExchangeStrategies.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build()).build().get().retrieve().bodyToMono(String.class).block();


                        JSONArray jsonArray = new JSONArray(indicatorDescription);

                        DbPublishedVersion dbPublishedVersion = getThePreviousIndicators(versionNumber);
                        assert dbPublishedVersion != null;

                        JSONObject jsonObject = null;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            JSONArray assessmentQuestionsArray = jsonObject.getJSONArray("assessmentQuestions");

                            for (int j = 0; j < assessmentQuestionsArray.length(); j++) {
                                JSONObject question = assessmentQuestionsArray.getJSONObject(j);
                                String name = question.getString("name");

                                for (DbIndicators dbIndicator : dbPublishedVersion.getDetails()) {
                                    for (DbIndicatorValues indicatorValue : dbIndicator.getIndicators()) {
                                        List<DbIndicatorDataValues> indicatorDataValues = indicatorValue.getIndicatorDataValue();
                                        for (DbIndicatorDataValues dbIndicatorDataValues : indicatorDataValues) {
                                            if (dbIndicatorDataValues.getName().equals(name)) {

                                                String indicatorId = question.getString("id");
                                                String code = question.getString("code");

                                                dbIndicatorDataValues.setId(indicatorId);
                                                dbIndicatorDataValues.setCode(code);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (dbPublishedVersion != null) {
                            dbDataEntryResponse.setIndicators(dbPublishedVersion);
                        }

                    } else {
                        String versionNo = getCurrentVersion();
                        DbPublishedVersion dbPublishedVersion = getThePreviousIndicators(versionNo);
                        if (dbPublishedVersion != null) {
                            dbDataEntryResponse.setIndicators(dbPublishedVersion);
                        }
                    }


                    return new Results(200, dbDataEntryResponse);
                }
            }
        }

        return new Results(400, "Resource not found.");
    }

    private DbPublishedVersion getThePreviousIndicators(String versionNumber) {
        String publishedBaseUrl = envUrlConstants.getNATIONAL_PUBLISHED_VERSIONS() + versionNumber;
        DbMetadataJson dbMetadataJson = internationalTemplateService.getIndicators(publishedBaseUrl);
        if (dbMetadataJson != null) {
            DbPrograms dbPrograms = dbMetadataJson.getMetadata();
            if (dbPrograms != null) {
                return dbPrograms.getPublishedVersion();
            }
        }
        return null;
    }

    @Override
    public Results updateDataEntry(String id, DbDataEntryData dbDataEntryData) {

        Optional<DataEntry> optionalDataEntry = dataEntryRepository.findById(Long.valueOf(id));
        if (optionalDataEntry.isPresent()) {

            DataEntry dataEntry = optionalDataEntry.get();
            String status = dataEntry.getStatus();
            if (status.equals(PublishStatus.PUBLISHED.name())) {
                return new Results(400, "This has already been pushed and cannot be updated.");
            }

            String selectedPeriod = dbDataEntryData.getSelectedPeriod();
            boolean isPublished = dbDataEntryData.isPublished();
            String dateEntryDate = dbDataEntryData.getDataEntryDate();

            String statusValue = PublishStatus.DRAFT.name();
            if (isPublished) {
                statusValue = PublishStatus.PUBLISHED.name();
            }

            dataEntry.setStatus(status);

            if (selectedPeriod != null) dataEntry.setSelectedPeriod(selectedPeriod);
            if (dateEntryDate != null) dataEntry.setDataEntryDate(dateEntryDate);
            dataEntry.setStatus(statusValue);
            DataEntry dataEntryAdded = dataEntryRepository.save(dataEntry);

            List<DbDataEntryResponses> responsesList = dbDataEntryData.getResponses();
            for (DbDataEntryResponses dataEntryResponses : responsesList) {
                String indicator = dataEntryResponses.getIndicator();
                String response = dataEntryResponses.getResponse();
                String comment = dataEntryResponses.getComment();
                String attachment = dataEntryResponses.getAttachment();

                List<DataEntryResponses> dataEntryResponsesList = new ArrayList<>();
                Optional<DataEntryResponses> optionalDataEntryResponses = dataEntryResponsesRepository.findByIndicatorAndDataEntry(indicator, dataEntryAdded);
                if (optionalDataEntryResponses.isPresent()) {
                    DataEntryResponses dataEntryResponsesDb = optionalDataEntryResponses.get();

                    if (response != null) dataEntryResponsesDb.setResponse(response);
                    if (comment != null) dataEntryResponsesDb.setComment(comment);
                    if (attachment != null) dataEntryResponsesDb.setAttachment(response);

                    dataEntryResponsesList.add(dataEntryResponsesDb);
                }
                dataEntryResponsesRepository.saveAll(dataEntryResponsesList);

            }

            if (isPublished) {

                Long dataEntryId = dataEntry.getId();
                Optional<DataEntry> dataEntryOptional = dataEntryRepository.findById(dataEntryId);

                if (dataEntryOptional.isPresent()) {

                    DataEntry dataEntry1 = dataEntryOptional.get();
                    List<DataEntryResponses> dataEntryResponseList = dataEntryResponsesRepository.findByDataEntry(dataEntry);

                    List<DbDataEntryResponses> dataEntryResponsesList = new ArrayList<>();
                    for (DataEntryResponses dataEntryResponses : dataEntryResponseList) {
                        String indicator = dataEntryResponses.getIndicator();
                        String response = dataEntryResponses.getResponse();
                        String comment = dataEntryResponses.getComment();
                        String attachment = dataEntryResponses.getAttachment();
                        DbDataEntryResponses dbDataEntryResponses = new DbDataEntryResponses(indicator, response, comment, attachment);
                        dataEntryResponsesList.add(dbDataEntryResponses);
                    }

                    String surveyId = dbDataEntryData.getSurveyId();

                    DbDataEntryData dbDataEntryResponse = new DbDataEntryData(surveyId, dataEntry1.getOrgUnit(), dataEntry1.getSelectedPeriod(), true, dataEntry1.getDataEntryPersonId(), dataEntry1.getDataEntryDate(), dataEntryResponsesList, null);
                    saveEventData(dbDataEntryResponse);

                }


            }


            return new Results(200, dataEntryAdded);

        }

        return new Results(400, "Resource not found");
    }

    public Results confirmDataEntry(Long id) {

        Optional<DataEntry> optionalDataEntries = dataEntryRepository.findById(id);

        if (optionalDataEntries.isPresent()) {

            DataEntry dataEntries = optionalDataEntries.get();

            // change status to PUBLISHED
            dataEntries.setStatus(PublishStatus.PUBLISHED.name());

            //update on dB:
            dataEntryRepository.save(dataEntries);

            return new Results(200, dataEntries);
        } else {
            return new Results(400, "Resource not found, verification not successful");
        }
    }

    @Override
    public Results rejectDataEntry(Long id) {

        Optional<DataEntry> optionalDataEntries = dataEntryRepository.findById(id);

        if (optionalDataEntries.isPresent()) {

            DataEntry dataEntries = optionalDataEntries.get();

            // change status to REJECTED
            dataEntries.setStatus(PublishStatus.REJECTED.name());

            //update on dB:
            dataEntryRepository.save(dataEntries);

            return new Results(200, dataEntries);
        } else {
            return new Results(400, "Resource not found, rejection not successful");
        }
    }

    private String getCommentsUploads(String codeComments, List<DbDataElements> dataElementsList) {

        String id = "";
        for (DbDataElements dataElements : dataElementsList) {
            String code = dataElements.getCode();
            if (codeComments.equals(code)) {
                id = dataElements.getId();
                break;
            }
        }
        return id;
    }

    @Override
    public Results listDataEntry(int no, int size, String status, String dataEntryPersonId) {

        List<DbSubmissionsResponse> responseList = new ArrayList<>();
        List<DataEntry> dataEntryList = getPagedDataEntryData(no, size, "", "", status, dataEntryPersonId);

        for (DataEntry dataEntry : dataEntryList) {

            List<String> stringList = new ArrayList<>();
            List<DataEntryResponses> dataEntryResponseList = dataEntryResponsesRepository.findByDataEntry(dataEntry);

            for (DataEntryResponses dataEntryResponses : dataEntryResponseList) {

                String indicator = dataEntryResponses.getIndicator();
                stringList.add(indicator);

            }

            String versionNumber = dataEntry.getVersionNumber();
            String url = envUrlConstants.getNATIONAL_PUBLISHED_VERSIONS() + versionNumber;
            DbMetadataJson dbMetadataJson = internationalTemplateService.getIndicators(url);
            DbPrograms dbPrograms = dbMetadataJson.getMetadata();
            List<DbIndicators> dbIndicatorsArrayList = new ArrayList<>();

            //fetch the data of the person who made the entry and add it to a list::>>>

            Optional<DataEntry> optionalDataEntry = dataEntryRepository.findById(dataEntry.getId());
            List<DataEntryPerson> dataEntryPersonList = new ArrayList<>();

            if (optionalDataEntry.isPresent()) {
                DataEntry dataEntryFound = optionalDataEntry.get();

                DataEntryPerson dataEntryPerson = new DataEntryPerson(dataEntryFound.getUsername(), dataEntryFound.getId().toString(), dataEntryFound.getSurname(), dataEntryFound.getFirstName(), dataEntryFound.getEmail());

                // Check for null values and assign them as-is
                if (dataEntryFound.getUsername() == null) {
                    dataEntryPerson.setUsername(null);
                }
                if (dataEntryFound.getId() == null) {
                    dataEntryPerson.setId(String.valueOf(0));
                }
                if (dataEntryFound.getSurname() == null) {
                    dataEntryPerson.setSurname(null);
                }
                if (dataEntryFound.getFirstName() == null) {
                    dataEntryPerson.setFirstName(null);
                }
                if (dataEntryFound.getEmail() == null) {
                    dataEntryPerson.setEmail(null);
                }

                dataEntryPersonList.add(dataEntryPerson);

            }


            if (dbPrograms != null) {
                DbPublishedVersion dbPublishedVersion = dbPrograms.getPublishedVersion();
                if (dbPublishedVersion != null) {

                    List<DbIndicators> dbIndicatorsList = dbPublishedVersion.getDetails();
                    for (DbIndicators dbIndicators : dbIndicatorsList) {

                        List<DbIndicatorValues> dbIndicatorValuesList = new ArrayList<>();
                        List<DbIndicatorValues> indicatorValuesList = dbIndicators.getIndicators();
                        for (DbIndicatorValues dbIndicatorValues : indicatorValuesList) {

                            List<DbIndicatorDataValues> dbIndicatorDataValuesList = new ArrayList<>();
                            List<DbIndicatorDataValues> dataValuesList = dbIndicatorValues.getIndicatorDataValue();
                            for (DbIndicatorDataValues dbIndicatorDataValues : dataValuesList) {
                                String indicatorId = (String) dbIndicatorDataValues.getId();
                                if (stringList.contains(indicatorId)) {

                                    DbIndicatorDataValues indicatorDataValues = new DbIndicatorDataValues(indicatorId, dbIndicatorDataValues.getCode(), dbIndicatorDataValues.getName(), dbIndicatorDataValues.getValueType());
                                    dbIndicatorDataValuesList.add(indicatorDataValues);
                                }


                            }

                            if (!dbIndicatorDataValuesList.isEmpty()) {
                                DbIndicatorValues indicatorValues = new DbIndicatorValues(dbIndicatorValues.getDescription(), null, dbIndicatorValues.getCategoryId(), dbIndicatorValues.getCategoryName(), null, dbIndicatorValues.getIndicatorName(), null, null, dbIndicatorDataValuesList);
                                dbIndicatorValuesList.add(indicatorValues);
                            }
                        }

                        DbIndicators indicators = new DbIndicators(dbIndicators.getCategoryName(), dbIndicatorValuesList);
                        dbIndicatorsArrayList.add(indicators);
                    }

                }
            }

            DbPublishedVersion publishedVersion = new DbPublishedVersion(dbIndicatorsArrayList.size(), dbIndicatorsArrayList);


            DbSubmissionsResponse dbSubmissionsResponse = new DbSubmissionsResponse(dataEntry.getId(), dataEntry.getSelectedPeriod(), dataEntry.getStatus(), dataEntry.getDataEntryPersonId(), dataEntry.getDataEntryDate(), dataEntry.getCreatedAt(), dataEntryPersonList);
            responseList.add(dbSubmissionsResponse);
        }


        DbResults dbResults = new DbResults(responseList.size(), responseList);

        return new Results(200, dbResults);
    }

    private List<DataEntry> getPagedDataEntryData(int pageNo, int pageSize, String sortField, String sortDirection, String status, String userId) {
        String sortPageField = "";
        String sortPageDirection = "";

        if (sortField.equals("")) {
            sortPageField = "createdAt";
        } else {
            sortPageField = sortField;
        }
        if (sortDirection.equals("")) {
            sortPageDirection = "DESC";
        } else {
            sortPageDirection = sortField;
        }

        Sort sort = sortPageDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortPageField).ascending() : Sort.by(sortPageField).descending();
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<DataEntry> page;
        if (status.equals("ALL") && (userId == null || userId.equals(""))) {
            page = dataEntryRepository.findAll(pageable);
        } else if (status.equals("ALL") && (userId != null && !userId.equals(""))) {
            page = dataEntryRepository.findAllByDataEntryPersonId(userId, pageable);
        } else {
            page = dataEntryRepository.findAllByStatus(status, pageable);
        }

        return page.getContent();
    }


    @Override
    public Results resendRoutineDataEntry(Long id, DbResendDataEntry resendDataEntry) {

        Optional<DataEntry> optionalDataEntry = dataEntryRepository.findById(id);
        if (optionalDataEntry.isPresent()) {

            DataEntry dataEntry = optionalDataEntry.get();
            List<DataEntryResponses> dataEntryResponseList = dataEntryResponsesRepository.findByDataEntry(dataEntry);
            DbDataEntryResponse dbDataEntryResponse = new DbDataEntryResponse(dataEntry.getId(), dataEntry.getSelectedPeriod(), dataEntry.getStatus(), dataEntry.getDataEntryPersonId(), dataEntry.getDataEntryDate(), dataEntry.getCreatedAt(), dataEntryResponseList, null, null);

            String versionNumber = dataEntry.getVersionNumber();
            if (versionNumber != null) {

                DbPublishedVersion dbPublishedVersion = getThePreviousIndicators(versionNumber);
                if (dbPublishedVersion != null) {
                    dbDataEntryResponse.setIndicators(dbPublishedVersion);
                }

                //change data entry status to 'REVISED'
                dataEntry.setStatus(PublishStatus.REVISED.name());

                //update on dB:
                dataEntryRepository.save(dataEntry);

            } else {
                String versionNo = getCurrentVersion();
                DbPublishedVersion dbPublishedVersion = getThePreviousIndicators(versionNo);
                if (dbPublishedVersion != null) {
                    dbDataEntryResponse.setIndicators(dbPublishedVersion);
                }

                //change data entry status to 'REVISED'
                dataEntry.setStatus(PublishStatus.REVISED.name());

                //update on dB:
                dataEntryRepository.save(dataEntry);
            }


            DbResendDataEntry dbResendDataEntry = new DbResendDataEntry("We have resent the data entry", resendDataEntry.getComments(), resendDataEntry.getIndicators());

            return new Results(200, dbResendDataEntry);

        }

        return new Results(400, "There was an issue with this request.");
    }


}
