package com.intellisoft.internationalinstance.service_impl.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.model.Response;
import com.intellisoft.internationalinstance.service_impl.service.IndicatorReferenceService;
import com.intellisoft.internationalinstance.util.AppConstants;
import com.intellisoft.internationalinstance.util.EnvUrlConstants;
import com.intellisoft.internationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Log4j2
@RequiredArgsConstructor
@Service
public class IndicatorReferenceImpl implements IndicatorReferenceService {

    @Value("${dhis.username}")
    private String username;
    @Value("${dhis.password}")
    private String password;

    private final FormatterClass formatterClass = new FormatterClass();

    private final EnvUrlConstants envUrlConstants;

    public Results addIndicatorDictionary(DbIndicatorDetails dbIndicatorDetails) {
        try {
            // Generate UUID
            String uuid = UUID.randomUUID().toString();

            // Add timestamp to dbIndicatorDetails object
            Instant now = Instant.now();
            Date date = Date.from(now);

            dbIndicatorDetails.setUuid(uuid);
            dbIndicatorDetails.setDate(date);

            // DHIS2 API urls:
            String url = envUrlConstants.getINDICATOR_DESCRIPTIONS();
            String createDataElementUrl = envUrlConstants.getCREATE_NEW_DATA_ELEMENT();

            // Retrieve existing JSON collection of Indicator_References from the INDICATOR_DESCRIPTIONS API
            List<DbIndicatorDetails> responseList = fetchExistingIndicatorDetails(url);

            if (responseList != null && !responseList.isEmpty()) {
                // Append new indicator details to the existing data dictionary
                responseList.add(dbIndicatorDetails);

                // Convert the updated list to JSON
                String updatedIndicatorList = convertToJson(responseList);

                // Send a PUT request to update the Indicator_References API with the updated JSON data
                if (updateIndicatorDetails(url, updatedIndicatorList)) {
                    // Indicator added successfully, now we create a dataElement from the assessmentQuestions

                    ObjectMapper objectMapper = new ObjectMapper();
                    // Create the new payload based on assessmentQuestions
                    ArrayNode dataElements = objectMapper.createArrayNode();

                    char alphabet = 'a';
                    for (DbAssessmentQuestion question : dbIndicatorDetails.getAssessmentQuestions()) {
                        // Fetch the optionSet
                        String optionSetId = question.getOptionSetId().toString();
                        String fetchOptionSetUrl = envUrlConstants.getFETCH_OPTION_SET_URL() + optionSetId;

                        OptionSet optionSet = fetchOptionSet(fetchOptionSetUrl);

                        // Create the dataElement object
                        ObjectNode dataElement = objectMapper.createObjectNode();
                        dataElement.put("aggregationType", DataElements.AVERAGE_SUM_ORG_UNIT.name());
                        dataElement.put("domainType", DataElements.AGGREGATE.name());
                        dataElement.put("valueType", (String) question.getValueType());
                        dataElement.put("name", (String) question.getName());

                        // Truncate the shortName to 25 characters
                        String shortName = String.valueOf(question.getName()).length() > 25 ? String.valueOf(question.getName()).substring(0, 25) : String.valueOf(question.getName());

                        dataElement.put("shortName", shortName);

                        // Append alphabet in incremental order to the code
                        String code = dbIndicatorDetails.getIndicatorReference() + "_" + alphabet;

                        dataElement.put("code", code);
                        dataElement.put("uid", uuid);

                        // Increment the alphabet
                        alphabet++;

                        // Add the optionSet to the dataElement
                        ObjectNode optionSetNode = objectMapper.createObjectNode();
                        optionSetNode.put("id", optionSetId);
                        dataElement.set("optionSet", optionSetNode);
                        dataElements.add(dataElement);
                    }

                    ObjectNode payload = objectMapper.createObjectNode();
                    payload.set("dataElements", dataElements);

                    // Convert the new payload to JSON string
                    String newPayloadString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);

                    System.out.println(newPayloadString);

                    // Make the API POST request with the new payload
                    if (postDataElement(createDataElementUrl, newPayloadString)) {

                        /**Add data elements to PSS Program**/

                        // loop through dataElements to get their names

                        // Create the payload object
                        ObjectNode addDataElementsToProgramPayload = objectMapper.createObjectNode();
                        ArrayNode dataElementArray = objectMapper.createArrayNode();

                        for (JsonNode dataElement : dataElements) {
                            String name = URLEncoder.encode(dataElement.get("name").asText(), StandardCharsets.UTF_8);
                            String nameFilter = "name:like:" + name;
                            String dataElementIdUrl = envUrlConstants.getFETCH_DATA_ELEMENTS_ID() + nameFilter;

                            System.out.print("dataElementIdUrl" + dataElementIdUrl);

                            // Make the API GET request to retrieve dataElements IDs
                            String dataElementIdResponse = fetchDataElementId(dataElementIdUrl);

                            // Process the dataElementResponse
                            JsonNode responseData = objectMapper.readTree(dataElementIdResponse);
                            JsonNode dataElementsNode = responseData.get("dataElements");
                            if (dataElementsNode.isArray() && dataElementsNode.size() > 0) {
                                for (JsonNode dataElementNode : dataElementsNode) {
                                    String id = dataElementNode.get("id").asText();
                                    System.out.println("Data Element ID: " + id);

                                    // Create a data element object with the ID
                                    ObjectNode dataElementObject = objectMapper.createObjectNode();
                                    dataElementObject.put("id", id);

                                    // Add the data element object to the array
                                    dataElementArray.add(dataElementObject);
                                }
                            } else {
                                System.out.println("Data Element not found for name: " + name);
                            }
                        }

                        payload.set("dataElements", dataElementArray);

                        // Convert the payload to JSON string
                        String payloadString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);

                        // make API call to add data elements to PSS Program:
                        String programId = AppConstants.PSS_PROGRAM_ID;

                        /**
                         * TODO: Check for right API for adding dataElements to Program
                         */
                        String addToProgramUrl = envUrlConstants.getADD_DATA_ELEMENTS_TO_PROGRAM() + programId + "/" + "dataElements";

                        System.out.println("addToProgramUrl " + addToProgramUrl);

                        if (!addDataElementsToProgram(addToProgramUrl, payloadString)) { //remove asterik once API FOUND:

                            // Create a new Data Element Group - If dealing with a new indicator

                            // Fetch data elements names & ids
                            JsonNode addDataElementsToGroupPayload = null;
                            for (JsonNode dataElement : dataElements) {
                                String name = URLEncoder.encode(dataElement.get("name").asText(), StandardCharsets.UTF_8);
                                String nameFilter = "name:like:" + name;
                                String dataElementIdUrl = envUrlConstants.getFETCH_DATA_ELEMENTS_ID() + nameFilter;

                                // Make the API GET request to retrieve dataElements IDs
                                String dataElementIdResponse = fetchDataElementId(dataElementIdUrl);

                                // Process the dataElementResponse
                                JsonNode responseData = objectMapper.readTree(dataElementIdResponse);
                                JsonNode dataElementsNode = responseData.get("dataElements");

                                // Create an array to store the data elements
                                List<JsonNode> dataElementsFound = new ArrayList<>();

                                // Iterate over the dataElementsNode and extract id and displayName
                                for (JsonNode elementNode : dataElementsNode) {
                                    String id = elementNode.get("id").asText();
                                    String displayName = elementNode.get("displayName").asText();

                                    // Create a new JSON object for each data element and add it to the array
                                    JsonNode dataElementFound = objectMapper.createObjectNode().put("id", id).put("name", displayName);
                                    dataElementsFound.add(dataElementFound);
                                }

                                // Create the final JSON payload
                                addDataElementsToGroupPayload = objectMapper.createObjectNode().put("name", (String) dbIndicatorDetails.getIndicatorName()).put("description", dbIndicatorDetails.getDescription()).put("compulsory", false).put("active", true).set("dataElements", objectMapper.valueToTree(dataElementsFound));
                            }

                            //convert payload to JSON:
                            String convertedPayload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(addDataElementsToGroupPayload);
                            System.out.println("convertedPayload" + convertedPayload);

                            //make POST call to create a dataElementGroup:
                            String createDataElementGroupUrl = envUrlConstants.getCREATE_DATA_ELEMENT_GROUP();

                            if (createDataElementGroup(createDataElementGroupUrl, convertedPayload)) {
                                //fetch dataElementGroups:
                                String fetchDataElementGroupUrl = envUrlConstants.getFETCH_DATA_ELEMENTS_URL();
                                DataElementGroupResponse responseMono = fetchDataElementGroups(fetchDataElementGroupUrl);

                                // Access the parsed data
                                responseMono.getDataElementGroups();
                                List<DataElementGroup> dataElementGroups = responseMono.getDataElementGroups();

                                for (DataElementGroup group : dataElementGroups) {
                                    String searchDisplayName = Objects.requireNonNull(dbIndicatorDetails.getIndicatorName()).toString();
                                    List<Map<String, Object>> foundDataElements = new ArrayList<>();

                                    //for (DataElementGroup dataElementGroup : dataElementGroups) {
                                    if (Objects.equals(group.getDisplayName(), searchDisplayName)) {
                                        Map<String, Object> dataElementMap = new HashMap<>();
                                        dataElementMap.put("id", group.getId());
                                        dataElementMap.put("name", group.getDisplayName());
                                        foundDataElements.add(dataElementMap);
                                    }
                                    //   }

                                    // Create the JSON request object
                                    Map<String, Object> addToGroupRequest = new HashMap<>();
                                    addToGroupRequest.put("dataElements", foundDataElements);

                                    // Convert the JSON request object to a JSON string
                                    try {
                                        String addToGroupPayload = new ObjectMapper().writeValueAsString(addToGroupRequest);
                                        System.out.println("addToGroupPayload" + addToGroupPayload);

                                        List<String> ids = new ArrayList<>();
                                        for (Map<String, Object> dataElementMap : foundDataElements) {
                                            String id = (String) dataElementMap.get("id");
                                            ids.add(id);
                                        }

                                        for (String id : ids) {
                                            //Add the dataElements to the Group created
                                            //post URL construction:
                                            String addDataElementsToGroupUrl = envUrlConstants.getCREATE_DATA_ELEMENT_GROUP() + "/" + id + "dataElements";

                                            if (addDataElementsToGroup(addDataElementsToGroupUrl, addToGroupPayload)) {
                                                return new Results(200, new DbDetails("The indicator values have been added."));
                                            }
                                        }

                                    } catch (JsonProcessingException | URISyntaxException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return new Results(400, "There was an issue processing your request.");
        } catch (IOException | URISyntaxException e) {
            log.error("An error occurred while processing the publishing workflow");
            return new Results(400, "Please try again after some time");
        }
    }

    private boolean addDataElementsToGroup(String addDataElementsToGroupUrl, String addToGroupPayload) throws URISyntaxException {
        try {
            var response = GenericWebclient.postForSingleObjResponse(addDataElementsToGroupUrl, addToGroupPayload, String.class, Response.class);
            return response.getHttpStatusCode() == 200;
        } catch (Exception e) {
            log.error("An error occurred while adding data elements to group");
            return false; // Return false in case of an exception
        }
    }

    private DataElementGroupResponse fetchDataElementGroups(String fetchDataElementGroupUrl) throws URISyntaxException {
        return GenericWebclient.getForSingleObjResponse(fetchDataElementGroupUrl, DataElementGroupResponse.class);
    }


    private boolean createDataElementGroup(String createDataElementGroupUrl, String convertedPayload) throws URISyntaxException {
        var response = GenericWebclient.postForSingleObjResponse(createDataElementGroupUrl, convertedPayload, String.class, Response.class);
//        return response.getHttpStatusCode() == 200;
        return true;
    }

    private boolean addDataElementsToProgram(String addToProgramUrl, String payloadString) throws URISyntaxException {
        try {
            var response = GenericWebclient.postForSingleObjResponse(addToProgramUrl, payloadString, String.class, Response.class);
            return response.getHttpStatusCode() == 200;
        } catch (Exception e) {
            log.error("An error occurred while adding data elements to program");
            return false; // Return false in case of an exception
        }
    }


    private String fetchDataElementId(String dataElementIdUrl) {
        try {
            String response = GenericWebclient.getForSingleObjResponse(dataElementIdUrl, String.class);
            return response;
        } catch (URISyntaxException e) {
            log.error("An error occurred while fetching data element IDs");
        }
        return null;
    }

    private OptionSet fetchOptionSet(String fetchOptionSetUrl) {
        WebClient webClient = WebClient.create();
        return webClient.get().uri(fetchOptionSetUrl).retrieve().bodyToMono(OptionSet.class).block();
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

    private boolean postDataElement(String createDataElementUrl, String newPayloadString) throws URISyntaxException {
        var response = GenericWebclient.postForSingleObjResponse(createDataElementUrl, newPayloadString, String.class, Response.class);
//        return response.getHttpStatusCode() == 200;
        return true;
    }

    private boolean updateProgramStage(String addDataElementToPssUrl, String programStagePayloadString) throws URISyntaxException {
        var response = GenericWebclient.putForSingleObjResponse(addDataElementToPssUrl, programStagePayloadString, String.class, Response.class);
        return response.getHttpStatusCode() == 200;
    }

    private boolean postDataElementToGroup(String addDataElementstoGroupUrl, String dataElementGroupPayloadString) throws URISyntaxException {
        var response = GenericWebclient.postForSingleObjResponse(addDataElementstoGroupUrl, dataElementGroupPayloadString, String.class, Response.class);
        return response.getHttpStatusCode() == 200;
    }

    private ObjectNode createFinalPayload(ArrayNode dataElements) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.putArray("dataElements").addAll(dataElements);

        return payload;
    }


    @Override
    public Results listIndicatorDictionary() {

        try {
            return new Results(200, getIndicatorList());
        } catch (Exception e) {
            log.error("An error occurred while fetching data dictionary listing");
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
            log.error("An error occurred while fetching indicator list");
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
                    DbMetadataValue dbMetadataValue = getMetadata(publishedBaseUrl + publishedVersionNo);
                    if (dbMetadataValue == null) {
                        return new Results(400, "There was an issue getting the published version.");
                    }

                    DbMetadataJsonData dbMetadataJsonData = dbMetadataValue.getMetadata();

                    List<DbIndicatorDetails> detailsList = new ArrayList<>();
//                    dbIndicatorDetailsList.add(indicatorDetails);
//                    detailsList.addAll(dbIndicatorDetailsList);

                    dbMetadataJsonData.setIndicatorDetails(detailsList);

                    dbMetadataValue.setMetadata(dbMetadataJsonData);

                    var response = GenericWebclient.putForSingleObjResponseWithAuth(publishedBaseUrl + publishedVersionNo, dbMetadataValue, DbMetadataValue.class, Response.class, authHeader);
                    if (response.getHttpStatusCode() == 200) {
                        return new Results(200, new DbDetails("The indicators values have been updated."));
                    }
                    return new Results(400, "There was an issue adding the resource");


                }

            }


        } catch (Exception e) {
            log.error("An error occurred while updating data dictionary");
        }

        return new Results(400, "There was an issue processing the request. Please try again.");
    }

    @Override
    public Results deleteDictionary(String uid) {

        try {

            String publishedBaseUrl = envUrlConstants.getDATA_STORE_ENDPOINT();
            int publishedVersionNo = getVersions(publishedBaseUrl);

            //Get metadata json
            DbMetadataValue dbMetadataValue = getMetadata(publishedBaseUrl + publishedVersionNo);
            if (dbMetadataValue == null) {
                return new Results(400, "There was an issue getting the published version.");
            }
            DbMetadataJsonData dbMetadataJsonData = dbMetadataValue.getMetadata();
            List<DbIndicatorDetails> dbIndicatorDetailsList = dbMetadataJsonData.getIndicatorDetails();

            if (dbIndicatorDetailsList != null) {

                for (DbIndicatorDetails dbIndicatorDetails : dbIndicatorDetailsList) {
                    String uuid = (String) dbIndicatorDetails.getUuid();
                    if (uid.equals(uuid)) {
                        dbIndicatorDetailsList.remove(dbIndicatorDetails);
                        break;
                    }
                }
                dbMetadataJsonData.setIndicatorDetails(dbIndicatorDetailsList);

                dbMetadataValue.setMetadata(dbMetadataJsonData);

                String auth = username + ":" + password;
                byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
                String authHeader = "Basic " + new String(encodedAuth);

                var response = GenericWebclient.putForSingleObjResponseWithAuth(publishedBaseUrl + publishedVersionNo, dbMetadataValue, DbMetadataValue.class, Response.class, authHeader);
                if (response.getHttpStatusCode() == 200) {
                    return new Results(200, new DbDetails("The indicator has been deleted."));
                }
                return new Results(400, "There was an issue adding the resource");


            }


        } catch (Exception e) {
            log.error("An error occurred while deleting from the data dictionary");
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

    private int getVersions(String url) throws URISyntaxException {

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

    private DbMetadataValue getMetadata(String publishedBaseUrl) {

        try {

            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);

            DbMetadataValue dbMetadataValue = WebClient.builder().baseUrl(publishedBaseUrl).defaultHeader(HttpHeaders.AUTHORIZATION, authHeader).build().get().retrieve().bodyToMono(DbMetadataValue.class).block();

            if (dbMetadataValue != null) {
                return dbMetadataValue;
            }

        } catch (Exception e) {
            log.error("An error occurred while fetching metadata");
        }
        return null;
    }

}