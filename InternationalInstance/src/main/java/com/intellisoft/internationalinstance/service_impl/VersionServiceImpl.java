package com.intellisoft.internationalinstance.service_impl;

import com.google.common.collect.Lists;
import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.db.Indicators;
import com.intellisoft.internationalinstance.db.MetadataJson;
import com.intellisoft.internationalinstance.db.VersionEntity;
import com.intellisoft.internationalinstance.db.repso.IndicatorsRepo;
import com.intellisoft.internationalinstance.db.repso.VersionRepos;
import com.intellisoft.internationalinstance.exception.CustomException;
import com.intellisoft.internationalinstance.model.Response;
import com.intellisoft.internationalinstance.util.AppConstants;
import com.intellisoft.internationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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

    @Override
    public Results getIndicators() throws URISyntaxException {


//        List<IndicatorForFrontEnd> indicatorForFrontEnds = new LinkedList<>();
        List<DbFrontendIndicators> indicatorForFrontEnds = new LinkedList<>();

        try{

            metadataJsonService.getMetadataData();
            getDataFromRemote();

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


        DbResults dbResults = new DbResults(
                categoryIndicatorsList.size(),
                categoryIndicatorsList);

        return new Results(200, dbResults);

    }

    @Override
    public Results saveDraftOrPublish(DbVersionData dbVersionData) throws URISyntaxException {

        Results results;
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

        String versionNo = String.valueOf(getInternationalVersions());

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

            List<String> metaDataList = indicatorsRepo.findByIndicatorIds(indicatorList);

            if (!metaDataList.isEmpty()){

                JSONObject jsonObjectMetadataJson = getRawRemoteData();
                JSONArray dataElementsArray = new JSONArray();

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
                                    dataElementsArray.put(jsonObjectMetadata);
                                }
                                MetadataJson metadataJsonUploads = metadataJsonService.getMetadataJsonByCode(codeUploads);
                                if (metadataJsonUploads != null){
                                    String metadataDataUpload = metadataJsonUploads.getMetadata();
                                    JSONObject jsonObjectMetadata = new JSONObject(metadataDataUpload);
                                    dataElementsArray.put(jsonObjectMetadata);
                                }
                                JSONObject jsonObjectMetadata = new JSONObject(metadataDataPoint);
                                dataElementsArray.put(jsonObjectMetadata);

                            }
                        }

                    });

                }

                var groups = GenericWebclient.getForSingleObjResponse(AppConstants.METADATA_GROUPINGS, String.class);
                var indicatorDescriptions = GenericWebclient.getForSingleObjResponse(AppConstants.INDICATOR_DESCRIPTIONS, String.class);
                JSONArray jsonArray = new JSONArray(indicatorDescriptions);
                JSONObject jsonObjectGroups = new JSONObject(groups);

                JSONObject jsonObject = new JSONObject();

                jsonObjectMetadataJson.put("dataElements",dataElementsArray);

                jsonObjectMetadataJson.put("groups",jsonObjectGroups);
                jsonObjectMetadataJson.put("indicatorDescriptions",jsonArray);

                jsonObject.put("metadata",  jsonObjectMetadataJson);
                jsonObject.put("version", versionNumber);
                jsonObject.put("versionDescription", versionDescription);


                var response = GenericWebclient.postForSingleObjResponse(
                        AppConstants.DATA_STORE_ENDPOINT+Integer.parseInt(versionNumber),
                        jsonObject,
                        JSONObject.class,
                        Response.class);
                log.info("RESPONSE FROM REMOTE: {}",response.toString());
                if (response.getHttpStatusCode() < 200) {
                    results=new Results(400, "Unable to create/update record on data store"+response);

                }else {

                    version.setStatus(PublishStatus.PUBLISHED.name());
                    if (publishedBy != null){
                        version.setPublishedBy(publishedBy);
                    }
                }

            }else {
                results=new Results(400, "No indicators found for the ids given"+indicatorList);
            }


        }

//        versionRepos.save(version);

        return new Results(201, versionRepos.save(version));
    }

    private int getInternationalVersions() throws URISyntaxException {

        var response = GenericWebclient.getForSingleObjResponse(
                AppConstants.DATA_STORE_ENDPOINT,
                List.class);

        if (!response.isEmpty()){
            return formatterClass.getNextVersion(response);
        }else {
            return 1;
        }

    }

    @Override
    public Results getTemplates(int page, int size, String status) {


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
    public Results getVersion(long versionId) {

        Results results;

        Optional<VersionEntity> optionalVersionEntity =
                versionRepos.findById(versionId);
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

    private void getIndicatorGroupings(List<DbFrontendIndicators> indicatorForFrontEnds, JSONObject jsonObject) {
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
                indicatorId,
                categoryName,
                indicatorName,
                dbIndicatorsList);
        indicatorForFrontEnds.add(dbFrontendIndicators);
    }

    private List<VersionEntity> getPagedTemplates(
            int pageNo,
            int pageSize,
            String sortField,
            String sortDirection,
            String status
    ) {
        String sortPageField = "";
        String sortPageDirection = "";

        if (sortField.equals("")){sortPageField = "createdAt"; }else {sortPageField = sortField;}
        if (sortDirection.equals("")){sortPageDirection = "DESC"; }else {sortPageDirection = sortField;}

        Sort sort = sortPageDirection.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortPageField).ascending() : Sort.by(sortPageField).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<VersionEntity> page = versionRepos.findAllByStatus(status, pageable);

        return page.getContent();
    }

    /**
     * Get data from MASTER TEMPLATE from DHIS Datastore and save into local db
     * @return
     * @throws URISyntaxException
     */
    private List<Indicators> getDataFromRemote() throws URISyntaxException {

        List<Indicators> indicators = new LinkedList<>();

        var  res =GenericWebclient.getForSingleObjResponse(
                AppConstants.METADATA_GROUPINGS, String.class);

        JSONObject jsObject = new JSONObject(res);
//        JSONArray dataElements = jsObject.getJSONArray("dataElements");
        JSONArray dataElements = jsObject.getJSONArray("dataElementGroups");
        dataElements.forEach(element->{
            String  id = ((JSONObject)element).getString("id");

            Indicators indicator = new Indicators();
            indicator.setIndicatorId(id);
            indicator.setMetadata(element.toString());

            boolean isIndicator = indicatorsRepo.existsByIndicatorId(id);
            if (!isIndicator){
                indicators.add(indicator);
            }

        });


        return Lists.newArrayList(indicatorsRepo.saveAll(indicators));

    }



    private JSONObject getRawRemoteData() throws URISyntaxException {
        var  res =GenericWebclient.getForSingleObjResponse(AppConstants.METADATA_JSON_ENDPOINT, String.class);
        return new  JSONObject(res);
    }
}
