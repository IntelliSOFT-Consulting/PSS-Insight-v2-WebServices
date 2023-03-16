package com.intellisoft.nationalinstance.service_impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellisoft.nationalinstance.*;
import com.intellisoft.nationalinstance.DbVersionData;
import com.intellisoft.nationalinstance.PublishStatus;
import com.intellisoft.nationalinstance.Results;
import com.intellisoft.nationalinstance.db.Indicators;
import com.intellisoft.nationalinstance.db.MetadataJson;
import com.intellisoft.nationalinstance.db.VersionEntity;
import com.intellisoft.nationalinstance.db.repso.IndicatorsRepo;
import com.intellisoft.nationalinstance.db.repso.VersionRepos;
import com.intellisoft.nationalinstance.exception.CustomException;
import com.intellisoft.nationalinstance.model.IndicatorForFrontEnd;
import com.intellisoft.nationalinstance.model.Response;
import com.intellisoft.nationalinstance.util.AppConstants;
import com.intellisoft.nationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import java.net.URISyntaxException;
import java.util.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {
    private final IndicatorsRepo indicatorsRepo;
    private final VersionRepos versionRepos;
    private final FormatterClass formatterClass = new FormatterClass();
    private final MetadataJsonService metadataJsonService;
    private final IndicatorDescriptionService indicatorDescriptionService;

//    @Override
//    public List<IndicatorForFrontEnd> getIndicators() throws URISyntaxException {
//        List<Indicators> indicators = getDataFromRemote();
//        return extractIndicators(indicators);
//    }

    @Override
    public List<IndicatorForFrontEnd> extractIndicators(List<Indicators> indicators) {
        List<IndicatorForFrontEnd> indicatorForFrontEnds = new LinkedList<>();
        indicators.forEach(indicator -> {
            JSONObject jsonObject = new JSONObject(indicator.getMetadata());
            try {
                String id = jsonObject.getString("id");
                String code = jsonObject.getString("code");
                String formName = jsonObject.getString("formName");
                indicatorForFrontEnds.add(new IndicatorForFrontEnd(id, code, formName));
            } catch (JSONException e) {
                log.info(e.getMessage());
            }

        });
        return indicatorForFrontEnds;
    }



    @Override
    public VersionEntity saveDraftOrPublish(DbVersionData dbVersionData) throws URISyntaxException {

        VersionEntity version = new VersionEntity();

        String versionDescription = dbVersionData.getVersionDescription();
        boolean isPublished = dbVersionData.isPublished();
        List<String> indicatorList = dbVersionData.getIndicators();

        String createdBy = dbVersionData.getCreatedBy();
        String publishedBy = dbVersionData.getPublishedBy();

        String status = PublishStatus.DRAFT.name();
        if (isPublished){
            status = PublishStatus.PUBLISHED.name();
        }

        int versionNum = getVersions(AppConstants.DATA_STORE_ENDPOINT);
        String versionNo = String.valueOf(versionNum+1);


        //Generate versions
        if (dbVersionData.getVersionId() != null){
            long versionId = dbVersionData.getVersionId();
            var vs = versionRepos.findById(versionId);
            if (vs.isPresent()) {
                VersionEntity versionEntity = vs.get();
                versionEntity.setStatus(status);
                versionEntity.setVersionDescription(versionDescription);
                versionEntity.setIndicators(indicatorList);
                versionNo = versionEntity.getVersionName();

                version = versionEntity;
            }


        }

        String versionNumber = versionNo;
        //Set the version number
        version.setVersionName(versionNumber);
        version.setIndicators(indicatorList);
        version.setVersionDescription(versionDescription);
        version.setStatus(status);
        if (createdBy != null){
            version.setCreatedBy(createdBy);
        }

        //Check if we're required to publish
        if(isPublished){

            /**
             * From the indicator list get the particular data points
             * From the indicator id, get the metadata json and push datapoint, comments and uploads
             * Use the code to get the comment and the uploads
             */
            String url = AppConstants.DATA_STORE_ENDPOINT+Integer.parseInt(versionNumber);

            int versionIntNum = getVersions(AppConstants.DATA_STORE_ENDPOINT_INT);
            String internationalUrl = AppConstants.DATA_STORE_ENDPOINT_INT+(versionIntNum);

            List<String> metaDataList = indicatorsRepo.findByIndicatorIds(indicatorList);


            if (!metaDataList.isEmpty()){

                DbMetadataJson dbMetadataJson = getRawRemoteData(internationalUrl);
//                JSONArray dataElementsArray = new JSONArray();
                List<DbDataValuesData> dbDataValuesDataList = new ArrayList<>();
                List<DbFrontendIndicators> indicatorForFrontEnds = new LinkedList<>();

                for (String s : metaDataList){
                    JSONObject jsonObject = new JSONObject(s);

                    JSONArray dataElements = jsonObject.getJSONArray("dataElements");
                    dataElements.forEach(element->{

                        if (((JSONObject)element).has("id")){
                            String  indicatorId = ((JSONObject)element).getString("id");
                            MetadataJson metadataJson = metadataJsonService.getMetadataJson(indicatorId);
                            if (metadataJson != null){

                                String code = metadataJson.getCode();
                                String metadataDataPoint = metadataJson.getMetadata();

                                String codeComment = code+"_Comments";
                                String codeUploads = code+"_Uploads";

                                MetadataJson metadataJsonComment = metadataJsonService.getMetadataJsonByCode(codeComment);
                                if (metadataJsonComment != null){
                                    String metadataDataComment = metadataJsonComment.getMetadata();
                                    JSONObject jsonObjectMetadata = new JSONObject(metadataDataComment);
                                    DbDataValuesData dbDataValuesData = new DbDataValuesData(
                                            jsonObjectMetadata.getString("code"),
                                            jsonObjectMetadata.getString("lastUpdated"),
                                            jsonObjectMetadata.getString("id"),
                                            jsonObjectMetadata.getString("created"),
                                            jsonObjectMetadata.getString("name"),
                                            jsonObjectMetadata.getString("shortName"),
                                            jsonObjectMetadata.getString("aggregationType"),
                                            jsonObjectMetadata.getString("domainType"),
                                            jsonObjectMetadata.getString("valueType"),
                                            jsonObjectMetadata.getString("formName"),
                                            jsonObjectMetadata.getBoolean("zeroIsSignificant"),
                                            jsonObjectMetadata.getJSONObject("categoryCombo"),
                                            jsonObjectMetadata.getJSONObject("lastUpdatedBy"),
                                            jsonObjectMetadata.getJSONObject("sharing"),
                                            jsonObjectMetadata.getJSONObject("createdBy"),
                                            jsonObjectMetadata.getJSONArray("translations"),
                                            jsonObjectMetadata.getJSONArray("attributeValues"),
                                            jsonObjectMetadata.getJSONArray("legendSets"),
                                            jsonObjectMetadata.getJSONArray("aggregationLevels")

                                    );
                                    dbDataValuesDataList.add(dbDataValuesData);
//                                    dataElementsArray.put(jsonObjectMetadata);
                                }
                                MetadataJson metadataJsonUploads = metadataJsonService.getMetadataJsonByCode(codeUploads);
                                if (metadataJsonUploads != null){
                                    String metadataDataUpload = metadataJsonUploads.getMetadata();
                                    JSONObject jsonObjectMetadata = new JSONObject(metadataDataUpload);
                                    DbDataValuesData dbDataValuesData = new DbDataValuesData(
                                            jsonObjectMetadata.getString("code"),
                                            jsonObjectMetadata.getString("lastUpdated"),
                                            jsonObjectMetadata.getString("id"),
                                            jsonObjectMetadata.getString("created"),
                                            jsonObjectMetadata.getString("name"),
                                            jsonObjectMetadata.getString("shortName"),
                                            jsonObjectMetadata.getString("aggregationType"),
                                            jsonObjectMetadata.getString("domainType"),
                                            jsonObjectMetadata.getString("valueType"),
                                            jsonObjectMetadata.getString("formName"),
                                            jsonObjectMetadata.getBoolean("zeroIsSignificant"),
                                            jsonObjectMetadata.getJSONObject("categoryCombo"),
                                            jsonObjectMetadata.getJSONObject("lastUpdatedBy"),
                                            jsonObjectMetadata.getJSONObject("sharing"),
                                            jsonObjectMetadata.getJSONObject("createdBy"),
                                            jsonObjectMetadata.getJSONArray("translations"),
                                            jsonObjectMetadata.getJSONArray("attributeValues"),
                                            jsonObjectMetadata.getJSONArray("legendSets"),
                                            jsonObjectMetadata.getJSONArray("aggregationLevels")

                                    );
                                    dbDataValuesDataList.add(dbDataValuesData);
//                                    dataElementsArray.put(jsonObjectMetadata);
                                }
                                JSONObject jsonObjectMetadata = new JSONObject(metadataDataPoint);
                                DbDataValuesData dbDataValuesData = new DbDataValuesData(
                                        jsonObjectMetadata.getString("code"),
                                        jsonObjectMetadata.getString("lastUpdated"),
                                        jsonObjectMetadata.getString("id"),
                                        jsonObjectMetadata.getString("created"),
                                        jsonObjectMetadata.getString("name"),
                                        jsonObjectMetadata.getString("shortName"),
                                        jsonObjectMetadata.getString("aggregationType"),
                                        jsonObjectMetadata.getString("domainType"),
                                        jsonObjectMetadata.getString("valueType"),
                                        jsonObjectMetadata.getString("formName"),
                                        jsonObjectMetadata.getBoolean("zeroIsSignificant"),
                                        jsonObjectMetadata.getJSONObject("categoryCombo"),
                                        jsonObjectMetadata.getJSONObject("lastUpdatedBy"),
                                        jsonObjectMetadata.getJSONObject("sharing"),
                                        jsonObjectMetadata.getJSONObject("createdBy"),
                                        jsonObjectMetadata.getJSONArray("translations"),
                                        jsonObjectMetadata.getJSONArray("attributeValues"),
                                        jsonObjectMetadata.getJSONArray("legendSets"),
                                        jsonObjectMetadata.getJSONArray("aggregationLevels")

                                );
                                dbDataValuesDataList.add(dbDataValuesData);
//                                dataElementsArray.put(jsonObjectMetadata);

                            }
                        }

                    });
                    getIndicatorGroupings(indicatorForFrontEnds, jsonObject);

                }
                List<DbFrontendCategoryIndicators> categoryIndicatorsList = getCategorisedIndicators(indicatorForFrontEnds);

                DbPrograms dbPrograms = dbMetadataJson.getMetadata();
                dbPrograms.setDataElements(dbDataValuesDataList);
                dbPrograms.setPublishedGroups(categoryIndicatorsList);


//
                DbMetadataJson dbMetadataJson1 = new DbMetadataJson(
                        versionNumber,
                        versionDescription,
                        dbPrograms
                );

//                jsonObject.put("metadata",  jsonObjectMetadataJson);
//                jsonObject.put("version", versionNumber);
//                jsonObject.put("versionDescription", versionDescription);


                var response = GenericWebclient.postForSingleObjResponse(
                        url,
                        dbMetadataJson1,
                        DbMetadataJson.class,
                        Response.class);

                log.info("RESPONSE FROM REMOTE: {}",response.toString());
                if (response.getHttpStatusCode() < 200) {
                    throw new CustomException("Unable to create/update record on data store"+response);
                }else {
//                    versionRepos.updateAllIsPublishedToFalse(PublishStatus.DRAFT.name());

                    version.setStatus(PublishStatus.PUBLISHED.name());
                    if (publishedBy != null){
                        version.setPublishedBy(publishedBy);
                    }
                }

            }else {
                throw new CustomException("No indicators found for the ids given"+indicatorList);
            }


        }

        return versionRepos.save(version);

    }

    private int getVersions(String url) throws URISyntaxException {

        var response = GenericWebclient.getForSingleObjResponse(
                url,
                List.class);

        if (!response.isEmpty()){
            return formatterClass.getNextVersion(response);
        }else {
            return 1;
        }

    }

    @Override
    public Results getTemplates(int page, int size, String status) {

//        List<VersionEntity> versionEntityList =
//                getPagedTemplates(
//                        page,
//                        size,
//                        "",
//                        "",
//                        status);

        List<VersionEntity> versionEntityList = (List<VersionEntity>) versionRepos.findAll();

        DbResults dbResults = new DbResults(
                versionEntityList.size(),
                versionEntityList);

        return new Results(200, dbResults);
    }

    @Override
    public Results deleteTemplate(long deleteId) {
        Results results;

        Optional<VersionEntity> optionalVersionEntity =
                versionRepos.findById(deleteId);
        if (optionalVersionEntity.isPresent()){
            versionRepos.deleteById(deleteId);
            results = new Results(200, new DbDetails(
                    optionalVersionEntity.get().getVersionName() + " has been deleted successfully."
            ));
        }else {
            results = new Results(400, "The id cannot be found.");
        }


        return results;
    }

    @Override
    public Results getVersion(String versionId) {
        Results results;

        Optional<VersionEntity> optionalVersionEntity =
                versionRepos.findById(Long.valueOf(versionId));
        List<DbFrontendIndicators> indicatorForFrontEnds = new LinkedList<>();

        if (optionalVersionEntity.isPresent()){

            VersionEntity versionEntity = optionalVersionEntity.get();
            List<String> entityIndicators = versionEntity.getIndicators();

            List<String> metaDataList = indicatorsRepo.findByIndicatorIds(entityIndicators);

            try {

                for(int j = 0; j < metaDataList.size(); j++){
                    String s = metaDataList.get(j);
                    JSONObject jsonObject = new JSONObject(s);
                    getIndicatorGroupings(indicatorForFrontEnds, jsonObject);
                }

            } catch (JSONException e) {
                System.out.println("*****1");
                e.printStackTrace();
            }

            List<DbFrontendCategoryIndicators> categoryIndicatorsList = getCategorisedIndicators(indicatorForFrontEnds);

            DbIndicatorValues dbIndicatorValues = new DbIndicatorValues(
                    versionEntity.getVersionName(),
                    versionEntity.getVersionDescription(),
                    versionId,
                    versionEntity.getStatus(),
                    categoryIndicatorsList);

            results = new Results(200, dbIndicatorValues);

        }else {
            results = new Results(400, "Version could not be found.");
        }
        return results;
    }

    @Override
    public Results getPublishedIndicators() throws URISyntaxException {

        DbMetadataJson dbMetadataJson = getPublishedData();
        Object publishedGroups=  dbMetadataJson.getMetadata().getPublishedGroups();
        if (publishedGroups != null){
            return new Results(200, publishedGroups);
        }else {
            return new Results(400, "No published indicators could be found.");
        }
    }

    private DbMetadataJson getPublishedData() throws URISyntaxException {
        String versionNo = String.valueOf(getVersions(AppConstants.DATA_STORE_ENDPOINT));
        String url = AppConstants.DATA_STORE_ENDPOINT+versionNo;
        return getRawRemoteData(url);
    }

    @Override
    public Results getIndicators() throws URISyntaxException {

        //Get versionNumber
        String versionNo = String.valueOf(getVersions(AppConstants.DATA_STORE_ENDPOINT_INT));
        String url = AppConstants.DATA_STORE_ENDPOINT_INT+versionNo;

        String publishedJson = GenericWebclient.getForSingleObjResponse(
                url, String.class);
        Gson gson = new Gson();
        JsonObject jsonObj = gson.fromJson (publishedJson, JsonObject.class);
        JsonObject metadataJson = jsonObj.getAsJsonObject("metadata");

        //get indicatorDescription object save using addIndicatorDescription in indicatorService
        JsonArray indicatorDescription = metadataJson.getAsJsonArray("indicatorDescriptions");
        indicatorDescriptionService.addIndicatorDescription(indicatorDescription);

        //get metadata dataelements and save using saveMetadataJson metadATaJsonService
        JsonArray dataElements = metadataJson.getAsJsonArray("dataElements");
        List<MetadataJson> metadataJsonList = new ArrayList<>();
        for (int i = 0; i < dataElements.size(); i++){
            JsonElement element = dataElements.get(i);
            String code = element.getAsJsonObject().get("code").getAsString();
            String id = element.getAsJsonObject().get("id").getAsString();

            MetadataJson metadataJsonDataElements = new MetadataJson();
            metadataJsonDataElements.setCode(code);
            metadataJsonDataElements.setId(id);
            metadataJsonDataElements.setMetadata(String.valueOf(element));
            metadataJsonList.add(metadataJsonDataElements);
        }
        metadataJsonService.saveMetadataJson(metadataJsonList);

        //get metadata groups and save using getMetadataFromRemote in this same class
        JsonArray groups = metadataJson.getAsJsonObject("groups").getAsJsonArray("dataElementGroups");
        for (int i = 0; i < groups.size(); i++){

            JsonElement element = groups.get(i);
            String indicatorId = element.getAsJsonObject().get("id").getAsString();
            String dataElementsValue = element.getAsJsonObject().toString();
            Indicators indicatorsData = new Indicators();
            indicatorsData.setIndicatorId(indicatorId);
            indicatorsData.setMetadata(dataElementsValue);

            Optional<Indicators> optionalIndicators = indicatorsRepo.findByIndicatorId(indicatorId);
            if (optionalIndicators.isPresent()){
                Indicators updateIndicator = optionalIndicators.get();
                updateIndicator.setMetadata(dataElementsValue);
                indicatorsRepo.save(updateIndicator);
            }else {
                indicatorsRepo.save(indicatorsData);
            }
        }

        //create a method to : will return json object
        //get versionNo for national
        //get metadata object for national passing the nationalVersion No
        //get groups from national

        List<DbFrontendIndicators> indicatorForFrontEnds = new LinkedList<>();

        try{
            List<Indicators> indicators = indicatorsRepo.findAll();

            indicators.forEach(indicator -> {
                JSONObject jsonObject = new JSONObject(indicator.getMetadata());
                try {
                    getIndicatorGroupings(indicatorForFrontEnds, jsonObject);

                } catch (JSONException e) {
                    System.out.println("*****1");
                    log.info(e.getMessage());
                }

            });

        }catch (Exception e){
            System.out.println("*****1");
            e.printStackTrace();
        }

        DbMetadataJson dbMetadataJson = getPublishedData();
        Object publishedGroups=  dbMetadataJson.getMetadata().getPublishedGroups();



        List<DbFrontendCategoryIndicators> categoryIndicatorsList = getCategorisedIndicators(indicatorForFrontEnds);
        DbHistoricalData dbHistoricalData = new DbHistoricalData(
                publishedGroups,
                categoryIndicatorsList
        );

//        DbResults dbResults = new DbResults(
//                categoryIndicatorsList.size(),
//                categoryIndicatorsList);

        return new Results(200, dbHistoricalData);

    }

    public List<DbFrontendCategoryIndicators> getCategorisedIndicators(List<DbFrontendIndicators> indicatorForFrontEnds){
        // Create a map to group the indicators by category name
        Map<String, List<DbFrontendIndicators>> groupedByCategory = new HashMap<>();
        for (DbFrontendIndicators indicator : indicatorForFrontEnds) {
            String categoryName = indicator.getCategoryName();
            if (!groupedByCategory.containsKey(categoryName)) {
                groupedByCategory.put(categoryName, new LinkedList<>());
            }
            groupedByCategory.get(categoryName).add(indicator);
        }

        // Create a new list of DbFrontendCategoryIndicators
        List<DbFrontendCategoryIndicators> categoryIndicatorsList = new LinkedList<>();
        for (String categoryName : groupedByCategory.keySet()) {
            List<DbFrontendIndicators> categoryIndicators = groupedByCategory.get(categoryName);

            DbFrontendCategoryIndicators category = new DbFrontendCategoryIndicators(categoryName, categoryIndicators);
            categoryIndicatorsList.add(category);
        }
        return categoryIndicatorsList;
    }

    public void getIndicatorGroupings(List<DbFrontendIndicators> indicatorForFrontEnds, JSONObject jsonObject) {

        String indicatorId = jsonObject.getString("id");
        String name  = jsonObject.getString("name");
        JSONArray dataElements = jsonObject.getJSONArray("dataElements");

        String indicatorName = formatterClass.getIndicatorName(name);
        String categoryName = formatterClass.mapIndicatorNameToCategory(name);

        List<DbIndicators> dbIndicatorsList = new ArrayList<>();

        for(int i = 0; i < dataElements.length(); i++){
            JSONObject jsonObject1 = dataElements.getJSONObject(i);

            if (jsonObject1.has("code") &&
                    jsonObject1.has("name") &&
                    jsonObject1.has("id")){
                String code = jsonObject1.getString("code");
                String formName = jsonObject1.getString("name");
                String formId = jsonObject1.getString("id");

                if (!code.contains("Comments") && !code.contains("Uploads")){
                    DbIndicators dbIndicators = new DbIndicators(code, formName, formId);
                    dbIndicatorsList.add(dbIndicators);
                }


            }

        }

        DbFrontendIndicators dbFrontendIndicators = new DbFrontendIndicators(
                name,
                indicatorId,
                categoryName,
                indicatorName,
                dbIndicatorsList);
        indicatorForFrontEnds.add(dbFrontendIndicators);
    }


    @Override
    public Response syncVersion() throws URISyntaxException {

        /**
         * Get the international data from the metadata json
         */
        var jsonObject = GenericWebclient.getForSingleObjResponse(
                AppConstants.INTERNATIONAL_METADATA_ENDPOINT,
                String.class);

        /**
         * TODO: 27/02/2023 post to national instance
         * Post the date to the National instance
         *
         */

        Response response = GenericWebclient.postForSingleObjResponse(
                AppConstants.DATA_STORE_ENDPOINT+ UUID.randomUUID().toString().split("-")[0],
                new JSONObject(jsonObject),
                JSONObject.class,
                Response.class);


        log.info("RESPONSE FROM REMOTE: {}",response.toString());
        if (response.getHttpStatusCode() < 200) {
            throw new CustomException("Unable to create/update record on data store"+response);
        }
        return response;

    }

    private DbMetadataJson getRawRemoteData(String url) throws URISyntaxException {
        //change to national url
        var  res =GenericWebclient.getForSingleObjResponse(url, DbMetadataJson.class);
        return res;
    }
}
