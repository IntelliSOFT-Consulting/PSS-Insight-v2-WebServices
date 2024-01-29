package com.intellisoft.internationalinstance.service_impl.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.db.NotificationEntity;
import com.intellisoft.internationalinstance.db.NotificationSubscription;
import com.intellisoft.internationalinstance.db.VersionEntity;
import com.intellisoft.internationalinstance.db.repso.NotificationSubscriptionRepo;
import com.intellisoft.internationalinstance.db.repso.VersionRepos;
import com.intellisoft.internationalinstance.model.Response;
import com.intellisoft.internationalinstance.service_impl.service.InternationalService;
import com.intellisoft.internationalinstance.service_impl.service.NotificationService;
import com.intellisoft.internationalinstance.util.AppConstants;
import com.intellisoft.internationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.io.File;
import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class InternationalServiceImpl implements InternationalService {

    private final FormatterClass formatterClass = new FormatterClass();
    private final VersionRepos versionRepos;
    private final NotificationService notificationService;
    private final NotificationSubscriptionRepo notificationSubscriptionRepo;
    @Value("${dhis.username}")
    private String username;
    @Value("${dhis.password}")
    private String password;

    public static String fetchIndicatorName(List<DbDataElements> dbDataElementsList, String indicatorCode) {
        for (DbDataElements dataElement : dbDataElementsList) {
            if (dataElement.getCode() != null && dataElement.getCode().equals(indicatorCode)) {
                return (String) dataElement.getName();
            }
        }
        return null;
    }

    @Override
    public Results getIndicators() {

        try {
            List<DbIndicatorsValue> dbIndicatorsValueList = getIndicatorsValues();
            return new Results(200, dbIndicatorsValueList);

        } catch (Exception e) {
            log.error("Error occurred while fetching indicators");
        }

        return new Results(400, "There was an error.");
    }

    public List<DbIndicatorsValue> getIndicatorsValues() {
        try {
            List<DbIndicatorsValue> dbIndicatorsValueList = new ArrayList<>();

            String url = AppConstants.METADATA_JSON_ENDPOINT;
            List<DbDataElements> dbDataElementsList = getDataElements(url);

            String groupUrl = AppConstants.METADATA_GROUPINGS;
            DbGroupsData dbGroupsData = GenericWebclient.getForSingleObjResponse(groupUrl, DbGroupsData.class);

            String indicatorDescriptionUrl = AppConstants.INDICATOR_DESCRIPTIONS;
            String indicatorDescription = GenericWebclient.getForSingleObjResponse(indicatorDescriptionUrl, String.class);
            JSONArray jsonArray = new JSONArray(indicatorDescription);

            if (dbGroupsData != null) {
                List<DbIndicatorDataValues> dbIndicatorDataValuesList = new ArrayList<>();

                List<DbGroupings> groupingsList = dbGroupsData.getDataElementGroups();
                for (DbGroupings dbGroupings : groupingsList) {

                    String categoryName = dbGroupings.getName();
                    String categoryId = dbGroupings.getId();
                    List<DbDataElementsData> dataElementsList = dbGroupings.getDataElements();

                    List<DbDataGrouping> dbDataGroupingList = new ArrayList<>();
                    for (DbDataElementsData dataElementsData : dataElementsList) {

                        String code = dataElementsData.getCode();
                        String name = dataElementsData.getName();
                        String id = dataElementsData.getId();

                        if (code != null) {
                            if (!code.contains("_Comment") && !code.contains("_Upload")) {
                                String valueType = getValueType(code, dbDataElementsList);
                                if (valueType != null) {
                                    DbDataGrouping dbDataGrouping = new DbDataGrouping(code, name, id, valueType);
                                    dbDataGroupingList.add(dbDataGrouping);
                                }
                            }

                        }

                    }

                    String indicatorName = fetchIndicatorName(dbDataElementsList, categoryName);

                    String description = "";
                    String uuid = null;
                    JSONObject jsonObject = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = jsonArray.getJSONObject(i);
                        uuid = jsonObject.getString("uuid");
                        JSONArray assessmentQuestionsArray = jsonObject.getJSONArray("assessmentQuestions");
                        if (jsonObject.has("Indicator_Code") && !jsonObject.isNull("Indicator_Code")) {
                            String Indicator_Code = jsonObject.getString("Indicator_Code");
                            if (categoryName.equals(Indicator_Code)) {
                                if (jsonObject.has("definition") && !jsonObject.isNull("definition")) {
                                    description = jsonObject.getString("definition");
                                }
                                break;
                            }
                        }
                    }

                    DbIndicatorDataValues dbIndicatorDataValues = new DbIndicatorDataValues(description, categoryId, categoryName, indicatorName, dbDataGroupingList, uuid);
                    dbIndicatorDataValuesList.add(dbIndicatorDataValues);

                }
                Map<String, List<DbIndicatorDataValues>> categoryMap = new HashMap<>();

                for (DbIndicatorDataValues dataValues : dbIndicatorDataValuesList) {
                    String name = (String) dataValues.getCategoryName();
                    String categoryName = formatterClass.mapIndicatorNameToCategory(name);

                    if (!categoryName.equals("Others")) {
                        if (!categoryMap.containsKey(categoryName)) {
                            categoryMap.put(categoryName, new ArrayList<>());
                        }

                        categoryMap.get(categoryName).add(dataValues);
                    }
                }

                for (Map.Entry<String, List<DbIndicatorDataValues>> entry : categoryMap.entrySet()) {
                    String category = entry.getKey();
                    List<DbIndicatorDataValues> indicators = entry.getValue();
                    dbIndicatorsValueList.add(new DbIndicatorsValue(category, indicators));
                }
            }
            return dbIndicatorsValueList;
        } catch (Exception e) {
            log.error("Error occurred while fetching indicator values");
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
        if (isPublished) {
            status = PublishStatus.PUBLISHED.name();
        }

        VersionEntity versionEntity = new VersionEntity();
        Long versionId = dbVersionData.getVersionId();
        if (versionId != null) {
            Optional<VersionEntity> optionalVersionEntity = versionRepos.findById(versionId);
            if (optionalVersionEntity.isPresent()) {
                versionEntity = optionalVersionEntity.get();

                String versionEntityStatus = versionEntity.getStatus();
                if (versionEntityStatus.equals(PublishStatus.PUBLISHED.name())) {
                    return new Results(400, "You cannot edit a published version");
                }

            }
        }
        if (versionDescription != null) versionEntity.setVersionDescription(versionDescription);
        if (createdBy != null) versionEntity.setCreatedBy(createdBy);
        if (publishedBy != null) versionEntity.setPublishedBy(publishedBy);
        if (!indicatorList.isEmpty()) versionEntity.setIndicators(indicatorList);
        versionEntity.setStatus(status);
        VersionEntity savedVersionEntity = versionRepos.save(versionEntity);
        if (isPublished) {

            try {
                savedVersionEntity.setStatus(PublishStatus.AWAITING_PUBLISHING.name());
                versionRepos.save(versionEntity);

                String url = AppConstants.METADATA_JSON_ENDPOINT;

                formatterClass.startBackGroundTask(url, versionDescription, savedVersionEntity, this, indicatorList);

                sendNotification(versionEntity);


            } catch (Exception e) {
                log.error("An error occurred while processing the publishing template");
            }


        }

        return new Results(200, new DbDetails("Version request is being processed."));
    }

    private void sendNotification(VersionEntity savedVersionEntity) {
        String message = "A new template has been published by " + savedVersionEntity.getPublishedBy() + " from the international instance. " + "The new template has the following details: " + "Version Number: " +savedVersionEntity.getVersionName()+ "\n\n"+ "Version description: " + savedVersionEntity.getVersionDescription() + "\n\n" + "Number of indicators: " + savedVersionEntity.getIndicators().size();

        List<String> dbEmailList = new ArrayList<>();
        //Get subscribed email addresses
        List<NotificationSubscription> notificationSubscriptionList = notificationSubscriptionRepo.findAllByIsActive(true);
        for (NotificationSubscription notificationSubscription : notificationSubscriptionList) {
            String emailAddress = notificationSubscription.getEmail();
            String email = emailAddress.replaceAll("\\s", "");
            boolean isEmail = formatterClass.isEmailValid(email);
            if (isEmail) dbEmailList.add(email);

        }

        NotificationEntity notification = new NotificationEntity();
        notification.setTitle("New Version Published.");
        notification.setSender(savedVersionEntity.getPublishedBy());
        notification.setMessage(message);
        notification.setEmailList(dbEmailList);

        /**
         * TODO: Check for configurations
         */

        notificationService.createNotification(notification);
    }

    @Transactional
    public void pushMetadata(DbMetadataValue dbMetadataJsonData, VersionEntity savedVersionEntity) {
        String groupUrl = AppConstants.METADATA_GROUPINGS;
        String indicatorDescriptionUrl = AppConstants.INDICATOR_DESCRIPTIONS;

        try {

            ObjectMapper objectMapper = new ObjectMapper();

            var indicatorDescription = GenericWebclient.getForSingleObjResponse(indicatorDescriptionUrl, String.class);

            List<DbIndicatorDescription> indicatorDescriptionList = objectMapper.readValue(indicatorDescription, List.class);

            DbGroupsData groupings = GenericWebclient.getForSingleObjResponse(groupUrl, DbGroupsData.class);

            dbMetadataJsonData.getMetadata().setGroups(groupings);
            dbMetadataJsonData.getMetadata().setIndicatorDescriptions(indicatorDescriptionList);
            String versionNumber = dbMetadataJsonData.getVersion();

            var response = GenericWebclient.postForSingleObjResponse(AppConstants.DATA_STORE_ENDPOINT + Integer.parseInt(versionNumber), dbMetadataJsonData, DbMetadataValue.class, Response.class);

            if (response.getHttpStatusCode() < 200) {
//                throw new CustomException("Unable to create/update record on data store"+response);

            } else {

                savedVersionEntity.setVersionName(versionNumber);
                savedVersionEntity.setStatus(PublishStatus.PUBLISHED.name());
                versionRepos.save(savedVersionEntity);

                //Create pdf
                generatePdf(dbMetadataJsonData);

            }


        } catch (Exception e) {
            log.error("An error occurred while pushing metadata");
        }

    }

    private DbPdfData getPdfData(DbMetadataValue dbMetadataValue) {


        /**
         * TODO: Remember the indicator reference has not been saved
         */

        String title = "Pharmaceutical Products and Services";
        String versionName = dbMetadataValue.getVersion();
        String versionDescription = dbMetadataValue.getVersionDescription();
        DbMetadataJsonData dbMetadataJsonData = dbMetadataValue.getMetadata();

        List<Map<String, String>> indicatorDescriptionsList = (List<Map<String, String>>) dbMetadataJsonData.getIndicatorDescriptions();

        DbResults dbResults = (DbResults) dbMetadataJsonData.getPublishedVersion();
        if (dbResults != null) {

            List<DbPdfSubTitle> dbPdfSubTitleList = new ArrayList<>();
            List<DbIndicatorsValue> dbIndicatorsValueList = (List<DbIndicatorsValue>) dbResults.getDetails();
            for (DbIndicatorsValue dbIndicatorsValue : dbIndicatorsValueList) {
                String subtitle = (String) dbIndicatorsValue.getCategoryName();

                List<DbPdfValue> dbPdfValueList = new ArrayList<>();
                List<DbIndicatorDataValues> indicatorDataValuesList = dbIndicatorsValue.getIndicators();
                for (DbIndicatorDataValues dataValues : indicatorDataValuesList) {
                    String categoryName = (String) dataValues.getCategoryName();
                    String indicatorName = (String) dataValues.getIndicatorName();

                    if (indicatorName != null) {
                        DbPdfValue dbPdfValue = new DbPdfValue("Indicator Name", indicatorName); //rename camel case
                        dbPdfValueList.add(dbPdfValue);
                    }
                    if (categoryName != null) {
                        DbPdfValue dbPdfValueIndicator = new DbPdfValue("Pss Insight Indicator", categoryName);
                        dbPdfValueList.add(dbPdfValueIndicator);

                        if (indicatorDescriptionsList != null) {
                            String definition = getDescriptionByCode(categoryName, indicatorDescriptionsList);
                            if (definition != null) {
                                DbPdfValue dbPdfValue = new DbPdfValue("Definition", definition);
                                dbPdfValueList.add(dbPdfValue);
                            }
                        }
                    }
                    if (subtitle != null) {
                        DbPdfValue dbPdfValue = new DbPdfValue("Topic", subtitle);
                        dbPdfValueList.add(dbPdfValue);
                    }

                    List<String> questionList = new ArrayList<>();
                    List<DbDataGrouping> dbDataGroupingList = dataValues.getIndicatorDataValue();
                    for (DbDataGrouping dataGrouping : dbDataGroupingList) {
                        String name = dataGrouping.getName();
                        questionList.add(name);
                    }

                    if (!questionList.isEmpty()) {
                        String assessmentQuestions = String.join("\n", questionList);
                        DbPdfValue dbPdfValue = new DbPdfValue("Assessment Questions", assessmentQuestions); //rename camel case
                        dbPdfValueList.add(dbPdfValue);
                    }

//                    if (indicatorDetails != null){
//
//                        DbPdfValue d1 =getPdfValue(indicatorDetails.getPurposeAndIssues(), "Purpose and Issues:");
//                        DbPdfValue d2 =getPdfValue(indicatorDetails.getPreferredDataSources(), "Preferred Data Sources:");
//                        DbPdfValue d3 =getPdfValue(indicatorDetails.getMethodOfEstimation(), "Method of Estimation:");
//                        DbPdfValue d4 =getPdfValue(indicatorDetails.getProposedScoring(), "Proposed Scoring or Benchmarking:");
//                        DbPdfValue d5 =getPdfValue(indicatorDetails.getExpectedFrequencyDataDissemination(), "Expected Frequency of Data Dissemination:");
//                        DbPdfValue d6 =getPdfValue(indicatorDetails.getIndicatorReference(), "Indicator Reference Number(s):");
//                        DbPdfValue d7 =getPdfValue(indicatorDetails.getIndicatorSource(), "Indicator Source(s):");
//
//                        if (d1 != null) dbPdfValueList.add(d1);
//                        if (d2 != null) dbPdfValueList.add(d2);
//                        if (d3 != null) dbPdfValueList.add(d3);
//                        if (d4 != null) dbPdfValueList.add(d4);
//                        if (d5 != null) dbPdfValueList.add(d5);
//                        if (d6 != null) dbPdfValueList.add(d6);
//                        if (d7 != null) dbPdfValueList.add(d7);
//
//                    }


                }

                if (subtitle != null) {
                    DbPdfSubTitle dbPdfSubTitle = new DbPdfSubTitle(subtitle, dbPdfValueList);
                    dbPdfSubTitleList.add(dbPdfSubTitle);
                }
            }

            DbPdfData dbPdfData = new DbPdfData(title, versionName, versionDescription, dbPdfSubTitleList);
            return dbPdfData;

        }

        return null;

    }

    private DbPdfValue getPdfValue(Object object, String title) {
        if (object != null) {
            return new DbPdfValue(title, (String) object);
        }
        return null;
    }

    private void generatePdf(DbMetadataValue dbMetadataValue) {

        try {

            DbPdfData dbPdfData = getPdfData(dbMetadataValue);
            if (dbPdfData != null) {
                File file = formatterClass.generatePdfFile(dbPdfData);
                String id = createFileResource(file);
                String publishedBaseUrl = AppConstants.DATA_STORE_ENDPOINT;

                String publishedVersionNo = dbMetadataValue.getVersion();
                //Get metadata json
                String fileUrl = AppConstants.INTERNATIONAL_BASE_URL + "documents/" + id + "/data";
                dbMetadataValue.getMetadata().setReferenceSheet(id);

                var response = GenericWebclient.putForSingleObjResponse(publishedBaseUrl + publishedVersionNo, dbMetadataValue, DbMetadataValue.class, Response.class);
            }

        } catch (Exception e) {
            log.error("An error occurred while generating PDF");
        }

    }

    public String createFileResource(File file) {

        try {

            RestTemplate restTemplate = new RestTemplate();
            String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.add("Authorization", authHeader);

            // Create request body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<DbFileResources> responseEntity = restTemplate.postForEntity(AppConstants.FILES_RESOURCES_ENDPOINT, requestEntity, DbFileResources.class);
            int code = responseEntity.getStatusCodeValue();
            if (code == 202) {
                if (responseEntity.getBody() != null) {
                    if (responseEntity.getBody().getResponse() != null) {
                        if (responseEntity.getBody().getResponse().getFileResource() != null) {
                            String id = responseEntity.getBody().getResponse().getFileResource().getId();
                            if (id != null) {
                                String fileUrl = AppConstants.INTERNATIONAL_BASE_URL + "documents";

                                DbFileData dbFileData = new DbFileData(id, "UPLOAD_FILE", true, false, id);

                                var response = GenericWebclient.postForSingleObjResponse(fileUrl, dbFileData, DbFileData.class, DbFileResponse.class);
                                if (response.getHttpStatusCode() == 201) {
                                    if (response.getResponse() != null) {
                                        return response.getResponse().getUid();
                                    }
                                }
                            }

                        }
                    }
                }
            }


        } catch (Exception e) {
            log.error("An error occurred while creating file resource for upload");
        }
        return "";
    }


    public String getDescriptionByCode(String indicatorCode, List<Map<String, String>> indicatorDescriptionsList) {

        for (Map<String, String> indicatorDescription : indicatorDescriptionsList) {
            String code = indicatorDescription.get("Indicator_Code");
            if (code != null) {
                if (code.equals(indicatorCode)) {
                    return indicatorDescription.get("Description");
                }
            }
        }

        return null; // return null if no match is found
    }

    public int getInternationalVersions() {

        try {
            var response = GenericWebclient.getForSingleObjResponse(AppConstants.DATA_STORE_ENDPOINT, List.class);

            if (!response.isEmpty()) {
                return formatterClass.getNextVersion(response);
            } else {
                return 1;
            }


        } catch (Exception e) {
            return 1;
        }

    }

    private String getValueType(String code, List<DbDataElements> dbDataElementsList) {
        for (DbDataElements dataElements : dbDataElementsList) {
            String dataElementCode = dataElements.getCode();
            if (code.equals(dataElementCode)) {
                return dataElements.getValueType();
            }
        }
        return null;
    }

    private DbMetadataValue getMetadata(String publishedBaseUrl) {

        try {

            DbMetadataValue dbMetadataValue = GenericWebclient.getForSingleObjResponse(publishedBaseUrl, DbMetadataValue.class);
            if (dbMetadataValue != null) {
                return dbMetadataValue;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private List<DbDataElements> getDataElements(String url) {

        try {

            DbMetadataJsonData dbMetadataJsonData = GenericWebclient.getForSingleObjResponse(url, DbMetadataJsonData.class);
            if (dbMetadataJsonData != null) {
                return dbMetadataJsonData.getDataElements();
            }

        } catch (Exception e) {
            log.error("An error occurred while fetching data elements");
        }
        return Collections.emptyList();
    }
}