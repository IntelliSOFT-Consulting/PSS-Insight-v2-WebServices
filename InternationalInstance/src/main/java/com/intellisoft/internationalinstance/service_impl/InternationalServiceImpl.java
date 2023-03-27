package com.intellisoft.internationalinstance.service_impl;

import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.db.VersionEntity;
import com.intellisoft.internationalinstance.db.repso.VersionRepos;
import com.intellisoft.internationalinstance.exception.CustomException;
import com.intellisoft.internationalinstance.model.Response;
import com.intellisoft.internationalinstance.util.AppConstants;
import com.intellisoft.internationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InternationalServiceImpl implements InternationalService{

    @Value("${dhis.international}")
    private String internationalUrl;
    @Value("${dhis.programs}")
    private String programsUrl;
    @Value("${dhis.groups}")
    private String groupsUrl;
    @Value("${dhis.indicator}")
    private String indicatorUrl;
    private final FormatterClass formatterClass = new FormatterClass();
    private final VersionRepos versionRepos;

    @Override
    public Results getIndicators() {

        try{
            List<DbIndicatorsValue> dbIndicatorsValueList = getIndicatorsValues();
            return new Results(200, dbIndicatorsValueList);

        }catch (Exception e){
            e.printStackTrace();
        }

        return new Results(400, "There was an error.");
    }

    private List<DbIndicatorsValue> getIndicatorsValues(){
        try{
            String url = internationalUrl + programsUrl;
            List<DbDataElements> dbDataElementsList = getDataElements(url);

            String groupUrl = internationalUrl + groupsUrl;
            List<DbIndicatorsValue> dbIndicatorsValueList = new ArrayList<>();
            DbGroupsData dbGroupsData = GenericWebclient.getForSingleObjResponse(
                    groupUrl, DbGroupsData.class);
            if (dbGroupsData != null){
                List<DbGroupings> groupingsList = dbGroupsData.getDataElementGroups();
                for (DbGroupings dbGroupings : groupingsList){
                    List<DbDataElementsData> dataElementsList = dbGroupings.getDataElements();
                    String categoryName = dbGroupings.getName();
                    String categoryId = dbGroupings.getId();

                    List<DbDataGrouping> dbDataGroupingList = new ArrayList<>();

                    for (DbDataElementsData dataElementsData: dataElementsList){

                        String code = dataElementsData.getCode();
                        String name = dataElementsData.getName();
                        String id = dataElementsData.getId();
                        if(code != null){
                            if (!code.contains("_Comments") && !code.contains("_Uploads")){
                                String valueType = getValueType(code, dbDataElementsList);
                                if (valueType != null){
                                    DbDataGrouping dbDataGrouping = new DbDataGrouping(
                                            code,
                                            name,
                                            id,
                                            valueType);
                                    dbDataGroupingList.add(dbDataGrouping);
                                }
                            }

                        }

                    }
                    if (categoryName != null){
                        List<DbIndicatorDataValues> dbIndicatorDataValuesList = new ArrayList<>();
                        String indicatorName = formatterClass.getIndicatorName(categoryName);
                        String categoryNameData = formatterClass.mapIndicatorNameToCategory(categoryName);

                        DbIndicatorDataValues dbIndicatorDataValues = new DbIndicatorDataValues(
                                categoryId,
                                categoryName,
                                indicatorName,
                                dbDataGroupingList
                        );
                        dbIndicatorDataValuesList.add(dbIndicatorDataValues);

                        DbIndicatorsValue dbIndicatorsValue = new DbIndicatorsValue(
                                categoryNameData,
                                dbIndicatorDataValuesList);
                        dbIndicatorsValueList.add(dbIndicatorsValue);
                    }

                }
            }
            return dbIndicatorsValueList;
        }catch (Exception e){
            return Collections.emptyList();
        }

    }

    @Override
    public Results saveUpdate(DbVersionData dbVersionData) {

        String versionDescription = dbVersionData.getVersionDescription();
        boolean isPublished = dbVersionData.isPublished();
        List<String> indicatorList = dbVersionData.getIndicators();

        String createdBy = dbVersionData.getCreatedBy();
        String publishedBy = dbVersionData.getPublishedBy();

        String status = PublishStatus.DRAFT.name();
        if (isPublished){
            status = PublishStatus.PUBLISHED.name();
        }

        VersionEntity versionEntity = new VersionEntity();
        Long versionId = dbVersionData.getVersionId();
        if (versionId != null){
            Optional<VersionEntity> optionalVersionEntity = versionRepos.findById(versionId);
            if (optionalVersionEntity.isPresent()){
                versionEntity = optionalVersionEntity.get();
            }
        }

        versionEntity.setVersionDescription(versionDescription);
        versionEntity.setStatus(status);
        versionEntity.setCreatedBy(createdBy);
        versionEntity.setPublishedBy(publishedBy);
        versionEntity.setIndicators(indicatorList);

        VersionEntity savedVersionEntity = versionRepos.save(versionEntity);
        if (isPublished){

            try{
                String url = internationalUrl + programsUrl;

                List<DbIndicatorsValue> dbIndicatorsValueListNew = new ArrayList<>();

                List<DbIndicatorsValue> dbIndicatorsValueList = getIndicatorsValues();
                for (DbIndicatorsValue dbIndicatorsValue: dbIndicatorsValueList){

                    List<DbIndicatorDataValues> dbIndicatorDataValuesList = new ArrayList<>();
                    String categoryName = (String) dbIndicatorsValue.getCategoryName();

                    List<DbIndicatorDataValues> indicatorDataValuesList = dbIndicatorsValue.getIndicators();
                    for (DbIndicatorDataValues dbIndicatorDataValues: indicatorDataValuesList){
                        String categoryId = (String) dbIndicatorDataValues.getCategoryId();
                        if (indicatorList.contains(categoryId)){
                            dbIndicatorDataValuesList.add(dbIndicatorDataValues);
                        }
                    }

                    DbIndicatorsValue dbIndicatorsValueNew = new DbIndicatorsValue(
                            categoryName,
                            dbIndicatorDataValuesList
                    );
                    dbIndicatorsValueListNew.add(dbIndicatorsValueNew);

                }
                DbResults dbResults = new DbResults(
                        dbIndicatorsValueListNew.size(),
                        dbIndicatorsValueListNew);

                DbMetadataJsonData dbMetadataJsonData = GenericWebclient.getForSingleObjResponse(
                        url, DbMetadataJsonData.class);
                dbMetadataJsonData.setPublishedVersion(dbResults);

                String versionNo = String.valueOf(getInternationalVersions() + 1);

                DbMetadataValue dbMetadataJson = new DbMetadataValue(
                        versionNo,
                        versionDescription,
                        dbMetadataJsonData);

                pushMetadata(dbMetadataJson, savedVersionEntity);

            }catch (Exception e){
                e.printStackTrace();
            }



        }



        return new Results(200, versionEntity);
    }

    @Async
    void pushMetadata(DbMetadataValue dbMetadataJsonData, VersionEntity savedVersionEntity){
        String groupUrl = internationalUrl + groupsUrl;
        String indicatorDescriptionUrl = internationalUrl + indicatorUrl;

        try{
            DbMetadataJsonData indicatorDescription = GenericWebclient.getForSingleObjResponse(
                    indicatorDescriptionUrl, DbMetadataJsonData.class);
            DbMetadataJsonData groupings = GenericWebclient.getForSingleObjResponse(
                    groupUrl, DbMetadataJsonData.class);
            dbMetadataJsonData.getMetadata().setGroups(groupings);
            dbMetadataJsonData.getMetadata().setIndicatorDescriptions(indicatorDescription);
            String versionNumber = dbMetadataJsonData.getVersion();

            var response = GenericWebclient.postForSingleObjResponse(
                    AppConstants.DATA_STORE_ENDPOINT+Integer.parseInt(versionNumber),
                    dbMetadataJsonData,
                    DbMetadataValue.class,
                    Response.class);
            if (response.getHttpStatusCode() < 200) {
                throw new CustomException("Unable to create/update record on data store"+response);
            }else {

                savedVersionEntity.setVersionName(versionNumber);
                savedVersionEntity.setStatus(PublishStatus.PUBLISHED.name());
                versionRepos.save(savedVersionEntity);

            }


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private int getInternationalVersions() {

        try{
            var response = GenericWebclient.getForSingleObjResponse(
                    AppConstants.DATA_STORE_ENDPOINT,
                    List.class);

            if (!response.isEmpty()){
                return formatterClass.getNextVersion(response);
            }else {
                return 1;
            }


        }catch (Exception e){
            return 1;
        }

    }

    private String getValueType(String code,List<DbDataElements> dbDataElementsList ){
        for (DbDataElements dataElements : dbDataElementsList ){
            String dataElementCode = dataElements.getCode();
            if (code.equals(dataElementCode)){
                return dataElements.getValueType();
            }
        }
        return null;
    }

    private List<DbDataElements> getDataElements(String url){

        try{

            DbMetadataJsonData dbMetadataJsonData = GenericWebclient.getForSingleObjResponse(
                    url, DbMetadataJsonData.class);
            if (dbMetadataJsonData != null){
                return dbMetadataJsonData.getDataElements();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
