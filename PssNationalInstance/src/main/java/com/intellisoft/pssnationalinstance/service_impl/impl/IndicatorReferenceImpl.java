package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.model.Response;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import com.intellisoft.pssnationalinstance.service_impl.service.IndicatorReferenceService;
import com.intellisoft.pssnationalinstance.util.EnvUrlConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Log4j2
@RequiredArgsConstructor
@Service
public class IndicatorReferenceImpl implements IndicatorReferenceService {

    private final EnvUrlConstants envUrlConstants;
    private final FormatterClass formatterClass = new FormatterClass();
    @Value("${dhis.username}")
    private String username;
    @Value("${dhis.password}")
    private String password;

    @Override
    public Results addIndicatorDictionary(DbIndicatorDetails dbIndicatorDetails) {
        try {
            // Generate UUID
            String uuid = UUID.randomUUID().toString();

            // Add timestamp to dbIndicatorDetails object
            Instant now = Instant.now();
            Date date = Date.from(now);

            dbIndicatorDetails.setUuid(uuid);
            dbIndicatorDetails.setDate(date);

            String url = AppConstants.INDICATOR_DESCRIPTIONS;

            // Retrieve existing JSON collection of Indicator_References from the INDICATOR_DESCRIPTIONS API
            List<DbIndicatorDetails> responseList = fetchExistingIndicatorDetails(url);

            if (responseList != null && !responseList.isEmpty()) {
                // Append new indicator details to the existing data dictionary
                responseList.add(dbIndicatorDetails);

                // Convert the updated list to JSON
                String updatedIndicatorList = convertToJson(responseList);

                // Send a PUT request to update the Indicator_References API with the updated JSON data
                if (updateIndicatorDetails(url, updatedIndicatorList)) {
                    return new Results(200, new DbDetails("The indicator values have been added."));
                }
            }

            return new Results(400, "There was an issue processing your request.");
        } catch (IOException | URISyntaxException e) {
            log.error("There was an issue adding indicator values to dictionary");
            return new Results(400, "Please try again after some time");
        }
    }

    private List<DbIndicatorDetails> fetchExistingIndicatorDetails(String url) throws URISyntaxException {
        Flux<DbIndicatorDetails> indicatorFlux = GenericWebclient.getForCollectionResponse(url, DbIndicatorDetails.class);
        return indicatorFlux.collectList().block();
    }

    private String convertToJson(List<DbIndicatorDetails> indicatorList) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(indicatorList);
    }

    private boolean updateIndicatorDetails(String url, String updatedIndicatorList) throws URISyntaxException {
        var response = GenericWebclient.putForSingleObjResponse(url, updatedIndicatorList, String.class, Response.class);
        return response.getHttpStatusCode() == 200;
    }


    @Override
    public Results listIndicatorDictionary() {

        try {
            return new Results(200, getIndicatorList());
        } catch (Exception e) {
            log.error("There was an issue Fetching indicators from the data dictionary");
        }


        return new Results(400, "There was an issue with the request. Try again later.");
    }

    private List<DbIndicatorDetails> getIndicatorList() {
        try {
            //Auth-headers:
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);

            String url = envUrlConstants.getINDICATOR_DESCRIPTIONS();

            //Get metadata json
            Flux<DbIndicatorDetails> responseFlux = WebClient.builder().baseUrl(url).defaultHeader(HttpHeaders.AUTHORIZATION, authHeader).build().get().retrieve().bodyToFlux(DbIndicatorDetails.class);

            List<DbIndicatorDetails> responseList = responseFlux.collectList().block();


            if (responseList != null) {

                List<DbIndicatorDetails> dbIndicatorDicList = new ArrayList<>();

                for (DbIndicatorDetails dataElements : responseList) {

                    String Description = dataElements.getDescription();
                    String Indicator_Code = dataElements.getIndicator_Code();


                    dbIndicatorDicList.add(dataElements);
                }


                return dbIndicatorDicList;
            }

        } catch (Exception e) {
            log.error("An error occurred while fetching indicator descriptions");
        }
        return Collections.emptyList();

    }

    @Override
    public Results getIndicatorValues(String uid) {

        DbIndicatorDetails dbIndicatorDetails = getIndicator(uid);
        if (dbIndicatorDetails != null) {
            return new Results(200, dbIndicatorDetails);
        }

        return new Results(400, "Resource not found");
    }

    private DbIndicatorDetails getIndicator(String uid) {
        List<DbIndicatorDetails> dbIndicatorDetailsList = getIndicatorList();
        for (DbIndicatorDetails dbIndicatorDetails : dbIndicatorDetailsList) {

            String uuid = (String) dbIndicatorDetails.getUuid();
            if (uid.equals(uuid)) {
                return dbIndicatorDetails;
            }

        }
        return null;
    }

    @Override
    public Results updateDictionary(DbIndicatorDetails dbIndicatorDetails) {

        /**
         * TODO: UPDATE ASSESSMENT QUESTIONS
         */

        try {

            String indicatorName = (String) dbIndicatorDetails.getIndicatorName();
            String indicatorCode = (String) dbIndicatorDetails.getIndicatorCode();
            String dataType = (String) dbIndicatorDetails.getDataType();
            String topic = (String) dbIndicatorDetails.getTopic();
            String definition = (String) dbIndicatorDetails.getDefinition();
            List<DbAssessmentQuestion> assessmentQuestions = dbIndicatorDetails.getAssessmentQuestions();
            String purposeAndIssues = (String) dbIndicatorDetails.getPurposeAndIssues();
            String preferredDataSources = (String) dbIndicatorDetails.getPreferredDataSources();
            String methodOfEstimation = (String) dbIndicatorDetails.getMethodOfEstimation();
            String proposedScoring = (String) dbIndicatorDetails.getProposedScoring();
            String expectedFrequencyDataDissemination = (String) dbIndicatorDetails.getExpectedFrequencyDataDissemination();
            String indicatorReference = (String) dbIndicatorDetails.getIndicatorReference();
            DbCreatedBy createdBy = dbIndicatorDetails.getCreatedBy();


            String uid = (String) dbIndicatorDetails.getUuid();
            if (uid != null) {
                DbIndicatorDetails indicatorDetails = null;
                List<DbIndicatorDetails> dbIndicatorDetailsList = getIndicatorList();
                for (DbIndicatorDetails details : dbIndicatorDetailsList) {

                    String uuid = (String) details.getUuid();
                    if (uid.equals(uuid)) {
                        indicatorDetails = details;
                        dbIndicatorDetailsList.remove(details);
                        break;
                    }

                }

                if (indicatorDetails != null) {

                    if (indicatorName != null) indicatorDetails.setIndicatorName(indicatorName);
                    if (indicatorCode != null) indicatorDetails.setIndicatorCode(indicatorCode);
                    if (dataType != null) indicatorDetails.setDate(dataType);
                    if (topic != null) indicatorDetails.setTopic(topic);
                    if (definition != null) indicatorDetails.setDefinition(definition);
                    if (purposeAndIssues != null) indicatorDetails.setPurposeAndIssues(purposeAndIssues);
                    if (preferredDataSources != null) indicatorDetails.setPreferredDataSources(preferredDataSources);
                    if (methodOfEstimation != null) indicatorDetails.setMethodOfEstimation(methodOfEstimation);
                    if (proposedScoring != null) indicatorDetails.setProposedScoring(proposedScoring);
                    if (expectedFrequencyDataDissemination != null)
                        indicatorDetails.setExpectedFrequencyDataDissemination(expectedFrequencyDataDissemination);
                    if (indicatorReference != null) indicatorDetails.setIndicatorReference(indicatorReference);

                    String publishedBaseUrl = envUrlConstants.getDATA_STORE_ENDPOINT();
                    int publishedVersionNo = getVersions(publishedBaseUrl);

                    String auth = username + ":" + password;
                    byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
                    String authHeader = "Basic " + new String(encodedAuth);

                    //Get metadata json
                    DbMetadataJson dbMetadataJson = getMetadata(publishedBaseUrl + publishedVersionNo);
                    if (dbMetadataJson == null) {
                        return new Results(400, "There was an issue getting the published version.");
                    }

                    DbPrograms dbPrograms = dbMetadataJson.getMetadata();

                    List<DbIndicatorDetails> detailsList = new ArrayList<>();
//                    dbIndicatorDetailsList.add(indicatorDetails);
//                    detailsList.addAll(dbIndicatorDetailsList);

                    dbPrograms.setIndicatorDetails(detailsList);

                    dbMetadataJson.setMetadata(dbPrograms);

                    var response = GenericWebclient.putForSingleObjResponse(publishedBaseUrl + publishedVersionNo, dbMetadataJson, DbMetadataJson.class, Response.class);
                    if (response.getHttpStatusCode() == 200) {
                        return new Results(200, new DbDetails("The indicators values have been updated."));
                    }
                    return new Results(400, "There was an issue adding the resource");


                }

            }


        } catch (Exception e) {
            log.error("An error occurred while updating indicator descriptions");
        }

        return new Results(400, "There was an issue processing the request. Please try again.");
    }

    @Override
    public Results deleteDictionary(String uid) {

        try {

            String publishedBaseUrl = envUrlConstants.getDATA_STORE_ENDPOINT();
            int publishedVersionNo = getVersions(publishedBaseUrl);

            //Get metadata json
            DbMetadataJson dbMetadataJson = getMetadata(publishedBaseUrl + publishedVersionNo);
            if (dbMetadataJson == null) {
                return new Results(400, "There was an issue getting the published version.");
            }
            DbPrograms dbPrograms = dbMetadataJson.getMetadata();
            List<DbIndicatorDetails> dbIndicatorDetailsList = dbPrograms.getIndicatorDetails();

            if (dbIndicatorDetailsList != null) {

                for (DbIndicatorDetails dbIndicatorDetails : dbIndicatorDetailsList) {
                    String uuid = (String) dbIndicatorDetails.getUuid();
                    if (uid.equals(uuid)) {
                        dbIndicatorDetailsList.remove(dbIndicatorDetails);
                        break;
                    }
                }
                dbPrograms.setIndicatorDetails(dbIndicatorDetailsList);

                dbMetadataJson.setMetadata(dbPrograms);

                String auth = username + ":" + password;
                byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
                String authHeader = "Basic " + new String(encodedAuth);

                var response = GenericWebclient.putForSingleObjResponseWithAuth(publishedBaseUrl + publishedVersionNo, dbMetadataJson, DbMetadataJson.class, Response.class, authHeader);

                if (response.getHttpStatusCode() == 200) {
                    return new Results(200, new DbDetails("The indicator has been deleted."));
                }
                return new Results(400, "There was an issue deleting the resource");


            }


        } catch (Exception e) {
            log.error("An error occurred while deleting the entry from the data dictionary");
        }


        return null;
    }

    @Override
    public Results getTopics() {

        String[] myTopics = {"Selection", "Procurement", "Distribution", "Use", "Coordination and leadership", "Pharmaceutical Laws and Regulations", "Ethics, Transparency, and Accountability", "Inspection and Enforcement", "Product Assessment and Registration", "Quality and Safety Surveillance", "Innovation, Research & Development", "Intellectual Property & Trade", "Costing & Pricing", "Financial Risk Protection", "Expenditure Tracking & Monitoring", "Human Resource Development ", "Human Resource Management", "Information Policy and Data Standardization"};
        List<String> topicList = new ArrayList<>(Arrays.asList(myTopics));
        DbResults dbResults1 = new DbResults(topicList.size(), topicList);

        String[] dropDowns = {IndicatorDropDowns.TEXT.name(), IndicatorDropDowns.SELECTION.name(), IndicatorDropDowns.NUMBER.name(),};
        List<String> dropList = new ArrayList<>(Arrays.asList(dropDowns));
        DbResults dbResults2 = new DbResults(dropList.size(), dropList);


        DbIndicatorTypes dbIndicatorTypes = new DbIndicatorTypes(dbResults1, dbResults2);
        return new Results(200, dbIndicatorTypes);
    }

    private int getVersions(String url) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth);

        var response = WebClient.builder().baseUrl(url).defaultHeader(HttpHeaders.AUTHORIZATION, authHeader).build().get().retrieve().bodyToMono(List.class).block();
        if (!response.isEmpty()) {
            return formatterClass.getNextVersion(response);
        } else {
            return 1;
        }
    }

    private DbMetadataJson getMetadata(String publishedBaseUrl) {

        try {

            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);

            DbMetadataJson dbMetadataJson = WebClient.builder().baseUrl(publishedBaseUrl).defaultHeader(HttpHeaders.AUTHORIZATION, authHeader).build().get().retrieve().bodyToMono(DbMetadataJson.class).block();

            if (dbMetadataJson != null) {
                return dbMetadataJson;
            }

        } catch (Exception e) {
            log.error("An error occurred while fetching metadata from DHIS2 Datastore");
        }
        return null;
    }

}
