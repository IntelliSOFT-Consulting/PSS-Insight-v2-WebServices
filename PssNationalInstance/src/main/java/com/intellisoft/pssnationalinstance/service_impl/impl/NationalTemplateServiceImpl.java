package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.db.IndicatorEdits;
import com.intellisoft.pssnationalinstance.db.VersionEntity;
import com.intellisoft.pssnationalinstance.repository.VersionEntityRepository;
import com.intellisoft.pssnationalinstance.service_impl.service.IndicatorEditsService;
import com.intellisoft.pssnationalinstance.service_impl.service.InternationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.NationalTemplateService;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class NationalTemplateServiceImpl implements NationalTemplateService {

    private final FormatterClass formatterClass = new FormatterClass();

    private final InternationalTemplateService internationalTemplateService;
    private final VersionEntityRepository versionEntityRepository;
    private final IndicatorEditsService indicatorEditsService;

    @Override
    public Results getNationalPublishedVersion() {

        try{

            DbPublishedVersion publishedVersionValues =
                    nationalPublishedIndicators();
            if (publishedVersionValues != null){
                return new Results(200, publishedVersionValues);
            }

        } catch (Exception syntaxException){
            syntaxException.printStackTrace();
        }
        return new Results(400, "The national indicators could not be found.");

    }
    public DbPublishedVersion nationalPublishedIndicators(){
        String publishedBaseUrl = AppConstants.NATIONAL_PUBLISHED_VERSIONS;
        DbMetadataJson dbMetadataJson =
                internationalTemplateService.getPublishedData(publishedBaseUrl);
        if (dbMetadataJson != null){
            DbPrograms dbPrograms = dbMetadataJson.getMetadata();
            if (dbPrograms != null){
                return dbPrograms.getPublishedVersion();
            }
        }
        return null;
    }

    @Override
    public Results getIndicatorDescription(String pssCode) {

        String description = "Indicator Description";
        try{
            String publishedBaseUrlNational = AppConstants.NATIONAL_PUBLISHED_VERSIONS;
            DbMetadataJson dbMetadataJsonNational = getMetadata(publishedBaseUrlNational);

            if (dbMetadataJsonNational != null){
                DbPrograms dbPrograms = dbMetadataJsonNational.getMetadata();
                if (dbPrograms != null){
                    List<DbIndicatorDescription> indicatorDescriptionNational = dbPrograms.getIndicatorDescriptions();
                    description = getIndicatorDescription(pssCode, indicatorDescriptionNational);
                }

            }else {
                //Check from the international indicator description
                String publishedBaseUrlInternational = AppConstants.INTERNATIONAL_PUBLISHED_VERSIONS;
                DbMetadataJson dbMetadataJsonInternational = getMetadata(publishedBaseUrlInternational);
                if (dbMetadataJsonInternational != null){
                    DbPrograms dbPrograms = dbMetadataJsonInternational.getMetadata();
                    if (dbPrograms != null){
                        List<DbIndicatorDescription> indicatorDescriptionNational = dbPrograms.getIndicatorDescriptions();
                        description = getIndicatorDescription(pssCode, indicatorDescriptionNational);
                    }
                }
            }
            
        }catch (Exception e){
            e.printStackTrace();
            return new Results(400, "Indicator could not be found.");
        }

        return new Results(200, new DbDetails(description));
    }
    private String getIndicatorDescription(String code,
                                           List<DbIndicatorDescription> dbIndicatorDescriptionList){

        String description = "";
        for (DbIndicatorDescription indicatorDescription : dbIndicatorDescriptionList){
            if (indicatorDescription.getIndicator_Code().equals(code)){
                description = indicatorDescription.getDescription();
                break;
            }
        }
        return description;
    }

    private DbMetadataJson getMetadata(String url) {
        return internationalTemplateService.getPublishedData(url);
    }

    @Async
    public void savePublishedVersion(String createdBy, String versionId, List<DbVersionDate> indicatorList){
        try{

            String nationalPublishedUrl = AppConstants.NATIONAL_PUBLISHED_VERSIONS;

            String versionNumber = "1";
            try{
                int versionNo = internationalTemplateService
                        .getVersions(nationalPublishedUrl);
                versionNumber = String.valueOf(versionNo + 1);
            }catch (Exception e){
                e.printStackTrace();
            }

            List<DbIndicators> indicatorsList = new ArrayList<>();

            List<String> internationalIndicators = new ArrayList<>();
            List<String> nationalIndicators = new ArrayList<>();
            for (DbVersionDate dbVersionDate : indicatorList){

                boolean isInternational = Boolean.TRUE.equals(dbVersionDate.isInternational());
                String id = dbVersionDate.getId();
                if (isInternational){
                    internationalIndicators.add(id);
                }else {
                    nationalIndicators.add(id);
                }
            }

            /**
             * Get national metadata
             */
            DbPublishedVersion publishedNationalIndicators = nationalPublishedIndicators();
            if (publishedNationalIndicators != null){
                List<DbIndicators> indicatorValuesList =
                        getSelectedIndicators(
                                publishedNationalIndicators.getDetails(),
                                nationalIndicators);
                indicatorsList.addAll(indicatorValuesList);
            }

            /**
             * Get international metadata
             */
            String internationalPublishedUrl = AppConstants.INTERNATIONAL_PUBLISHED_VERSIONS;
            DbMetadataJson dbMetadataJson = getMetadata(internationalPublishedUrl);

            if (dbMetadataJson != null){

                DbPrograms dbPrograms = dbMetadataJson.getMetadata();
                if (dbPrograms != null){
                    DbPublishedVersion publishedVersionValues = dbPrograms.getPublishedVersion();
                    if (publishedVersionValues != null){
                        List<DbIndicators> indicatorValuesList =
                                getSelectedIndicators(
                                        publishedVersionValues.getDetails(),
                                        internationalIndicators);
                        indicatorsList.addAll(indicatorValuesList);
                    }

                }

                //Get updates from db and include them
                List<IndicatorEdits> indicatorEditsList =
                        indicatorEditsService.getIndicatorEditsCreator(createdBy);
                for (IndicatorEdits indicatorEdits: indicatorEditsList){

                    String categoryId = indicatorEdits.getCategoryId();
                    String indicatorId = indicatorEdits.getIndicatorId();
                    String edits = indicatorEdits.getEdit();

                    for (DbIndicators dbIndicators: indicatorsList){
                        List<DbIndicatorValues> indicators = dbIndicators.getIndicators();
                        for (DbIndicatorValues dbIndicatorValues: indicators){
                            String dbCategoryId = (String) dbIndicatorValues.getCategoryId();
                            List<DbIndicatorDataValues> indicatorDataValueList = dbIndicatorValues.getIndicatorDataValue();
                            if (categoryId.equals(dbCategoryId)){
                                if (categoryId.equals(indicatorId)){
                                    dbIndicatorValues.setIndicatorName(edits);
                                }else {
                                    for (DbIndicatorDataValues dbIndicatorDataValues : indicatorDataValueList){
                                        String dbIndicatorId = (String) dbIndicatorDataValues.getId();
                                        if (indicatorId.equals(dbIndicatorId)){
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
                DbPublishedVersion dbPublishedVersion = new DbPublishedVersion(
                        indicatorsList.size(),
                        indicatorsList);
                assert dbPrograms != null;
                dbPrograms.setPublishedVersion(dbPublishedVersion);
                dbMetadataJson.setMetadata(dbPrograms);
            }

            //Save the new Version

            var response = GenericWebclient.postForSingleObjResponse(
                    nationalPublishedUrl + versionNumber,
                    dbMetadataJson,
                    DbMetadataJson.class,
                    DbPublishVersionResponse.class);

            if (response.getHttpStatusCode() == 201) {
                Optional<VersionEntity> optionalVersionEntity =
                        versionEntityRepository.findById(Long.valueOf(versionId));
                if (optionalVersionEntity.isPresent()){
                    VersionEntity versionEntity = optionalVersionEntity.get();
                    versionEntity.setVersionName(versionNumber);
                    versionEntityRepository.save(versionEntity);
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<DbIndicators> getSelectedIndicators(
            List<DbIndicators> details,
            List<String> selectedIndicators){

        List<DbIndicators> dbIndicatorsList = new ArrayList<>();
        for (DbIndicators dbIndicators: details){
            String categoryName = (String) dbIndicators.getCategoryName();

            List<DbIndicatorValues> newIndicators = new ArrayList<>();
            List<DbIndicatorValues> indicatorValuesList = dbIndicators.getIndicators();
            for (DbIndicatorValues indicatorValues : indicatorValuesList) {
                if (selectedIndicators.stream().anyMatch(
                        indicatorValues.getCategoryId()
                                .toString()::contains)) {
                    newIndicators.add(indicatorValues);
                }
            }
            if (!newIndicators.isEmpty()){
                DbIndicators dbNewIndicators = new DbIndicators(
                        categoryName, newIndicators);
                dbIndicatorsList.add(dbNewIndicators);
            }
        }

        return dbIndicatorsList;
    }

    @Override
    public DbMetadataJson getPublishedMetadataJson() {
        String publishedBaseUrl = AppConstants.NATIONAL_PUBLISHED_VERSIONS;
        return internationalTemplateService.getPublishedData(publishedBaseUrl);
    }

}
