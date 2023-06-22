package com.intellisoft.internationalinstance.service_impl.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.model.Response;
import com.intellisoft.internationalinstance.service_impl.service.IndicatorReferenceService;
import com.intellisoft.internationalinstance.util.AppConstants;
import com.intellisoft.internationalinstance.util.FormulaUtil;
import com.intellisoft.internationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Flux;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import org.jdom2.Document;
import org.jdom2.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;

@RequiredArgsConstructor
@Service
public class IndicatorReferenceImpl implements IndicatorReferenceService {

    private final FormatterClass formatterClass = new FormatterClass();

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
            String createDataElementUrl = AppConstants.CREATE_DATA_ELEMENT;

            // Retrieve existing JSON collection of Indicator_References from the INDICATOR_DESCRIPTIONS API
            List<DbIndicatorDetails> responseList = fetchExistingIndicatorDetails(url);

            if (responseList != null && !responseList.isEmpty()) {
                // Append new indicator details to the existing data dictionary
                responseList.add(dbIndicatorDetails);

                // Convert the updated list to JSON
                String updatedIndicatorList = convertToJson(responseList);

                // Send a PUT request to update the Indicator_References API with the updated JSON data
                if (updateIndicatorDetails(url, updatedIndicatorList)) {

                    ObjectMapper objectMapper = new ObjectMapper();
                    // Create the new payload based on assessmentQuestions
                    ArrayNode dataElements = objectMapper.createArrayNode();

                    for (DbAssessmentQuestion question : dbIndicatorDetails.getAssessmentQuestions()) {
                        ObjectNode dataElement = objectMapper.createObjectNode();
                        dataElement.put("aggregationType", DataElements.AVERAGE_SUM_ORG_UNIT.name());
                        dataElement.put("domainType", DataElements.AGGREGATE.name());
                        dataElement.put("valueType", (String) question.getValueType());
                        dataElement.put("name", (String) question.getName());
                        dataElement.put("shortName", (String) question.getName());
                        dataElement.put("code", (String) question.getName());
                        dataElement.put("uid", uuid);
                        dataElements.add(dataElement);
                    }

                    ObjectNode payload = objectMapper.createObjectNode();
                    payload.set("dataElements", dataElements);

                    // Convert the new payload to JSON string
                    String newPayloadString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);

                    System.out.println(newPayloadString);

                    // Make the API POST request with the new payload
                    if (postDataElement(createDataElementUrl, newPayloadString)) {
                        // Add data elements to PSS program::

                        // Retrieve PSS programId from DHIS2 API
                        String programId = AppConstants.PSS_PROGRAM_ID;

                        String programStageId = AppConstants.PSS_PROGRAM_STAGE_ID;

                        // Create the new payload for program stage
                        ObjectNode programStagePayload = objectMapper.createObjectNode();
                        programStagePayload.put("name", "Program Stage Name"); // To be replaced with a valid name
                        ObjectNode programNode = objectMapper.createObjectNode();
                        programNode.put("id", programId);
                        programStagePayload.set("program", programNode);

                        ArrayNode programStageDataElements = objectMapper.createArrayNode();
                        for (DbAssessmentQuestion question : dbIndicatorDetails.getAssessmentQuestions()) {
                            ObjectNode dataElementNode = objectMapper.createObjectNode();
                            ObjectNode innerDataElementNode = objectMapper.createObjectNode();
                            innerDataElementNode.put("id", uuid);
                            dataElementNode.set("dataElement", innerDataElementNode);
                            programStageDataElements.add(dataElementNode);
                        }

                        programStagePayload.set("programStageDataElements", programStageDataElements);

                        // Convert the program stage payload to JSON string
                        String programStagePayloadString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(programStagePayload);

                        System.out.println("programStagePayloadString" + programStagePayloadString);

                        // Make the PUT request to update the program stage
                        String addDataElementToPssUrl = AppConstants.ADD_DATA_ELEMENT_TO_PSS_PROGRAM + "/" + programStageId;

                        if (updateProgramStage(addDataElementToPssUrl, programStagePayloadString)) {
                            /*
                             * Create a new Data Element Group - If dealing with a new indicator
                             * Use existing one if indicator already exists
                             * Add the new data elements to their respective Data Element Group.
                             */

                            // Fetch data element IDs
                            try {
                                // Create a URL object from the fetch data elements URL
                                URL fetchDataElementsURL = new URL(AppConstants.FETCH_DATA_ELEMENTS_URL);

                                // Open a connection to the URL
                                HttpURLConnection connection = (HttpURLConnection) fetchDataElementsURL.openConnection();
                                connection.setRequestMethod("GET");

                                // Get the response code
                                int responseCode = connection.getResponseCode();

                                if (responseCode == HttpURLConnection.HTTP_OK) {
                                    // Read the response
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    StringBuilder response = new StringBuilder();
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        response.append(line);
                                    }
                                    reader.close();

                                    // Parse the XML response
                                    SAXBuilder builder = new SAXBuilder();
                                    Document document = builder.build(new ByteArrayInputStream(response.toString().getBytes()));


                                    // Get the root element of the XML
                                    Element rootElement = document.getRootElement();

                                    // Get the "dataElements" element
                                    Element dataElementsElement = rootElement.getChild("dataElements");

                                    // Get all the child elements of "dataElements" (i.e., the individual data elements)
                                    List<Element> dataElementElements = dataElementsElement.getChildren();

                                    // Create a new list to store the data element IDs
                                    List<String> dataElementIds = new ArrayList<>();

                                    // Iterate over the data element elements and extract the IDs
                                    for (Element dataElementElement : dataElementElements) {
                                        String dataElementId = dataElementElement.getAttributeValue("id");
                                        dataElementIds.add(dataElementId);
                                    }

                                    // Update the payload for each assessment question
                                    for (DbAssessmentQuestion question : dbIndicatorDetails.getAssessmentQuestions()) {
                                        Map<String, Object> dataElementGroupPayload = new HashMap<>();

                                        // Set the properties of the data element group
                                        dataElementGroupPayload.put("name", dbIndicatorDetails.getIndicatorCode());
                                        dataElementGroupPayload.put("shortName", question.getName());
                                        dataElementGroupPayload.put("code", question.getName());
                                        dataElementGroupPayload.put("description", dbIndicatorDetails.getTopic());

                                        // Add the data element IDs to the payload
                                        dataElementGroupPayload.put("dataElements", dataElementIds);

                                        // Convert the payload to JSON string
                                        String dataElementGroupPayloadString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataElementGroupPayload);

                                        // Print the payload
                                        System.out.println(dataElementGroupPayloadString);

                                        // Make the POST request to add the new data elements to their respective Data Element Group
                                        String addDataElementstoGroupUrl = AppConstants.ADD_DATA_ELEMENTS_TO_GROUP;

                                        if(postDataElementToGroup(addDataElementstoGroupUrl, dataElementGroupPayloadString)){

                                            return new Results(200, new DbDetails("The indicator values have been added."));
                                        }
                                    }
                                } else {
                                    // Handle the error response
                                    System.out.println("Error: " + responseCode);
                                }
                                // Close the connection
                                connection.disconnect();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JDOMException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }
                }
            }

            return new Results(400, "There was an issue processing your request.");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
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

    private boolean postDataElement(String createDataElementUrl, String newPayloadString) throws URISyntaxException {
        var response = GenericWebclient.postForSingleObjResponse(createDataElementUrl, newPayloadString, String.class, Response.class);
        return response.getHttpStatusCode() == 200;
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

        try{
            return new Results(200, getIndicatorList());
        }catch (Exception e){
            e.printStackTrace();
        }


        return new Results(400, "There was an issue with the request. Try again later.");
    }

    private List<DbIndicatorDetails> getIndicatorList(){
        try{
            String url = AppConstants.INDICATOR_DESCRIPTIONS;
            //Get metadata json
            Flux<DbIndicatorDetails> responseFlux = GenericWebclient.getForCollectionResponse(
                    url,
                    DbIndicatorDetails.class
            );
            List<DbIndicatorDetails> responseList = responseFlux.collectList().block();


            if (responseList != null){

                List<DbIndicatorDetails> dbIndicatorDicList = new ArrayList<>();

                for (DbIndicatorDetails dataElements: responseList){

                    String Description = dataElements.getDescription();
                    String Indicator_Code = dataElements.getIndicator_Code();


                    dbIndicatorDicList.add(dataElements);
                }


                return dbIndicatorDicList;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return Collections.emptyList();

    }

    @Override
    public Results getIndicatorValues(String uid) {

        DbIndicatorDetails dbIndicatorDetails = getIndicator(uid);
        if (dbIndicatorDetails != null){
            return new Results(200, dbIndicatorDetails);
        }

        return new Results(400, "Resource not found");
    }
    private DbIndicatorDetails getIndicator(String uid){
        List<DbIndicatorDetails> dbIndicatorDetailsList = getIndicatorList();
        for (DbIndicatorDetails dbIndicatorDetails : dbIndicatorDetailsList){

            String uuid = (String) dbIndicatorDetails.getUuid();
            if (uid.equals(uuid)){
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

        try{

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
            if (uid != null){
                DbIndicatorDetails indicatorDetails = null;
                List<DbIndicatorDetails> dbIndicatorDetailsList = getIndicatorList();
                for (DbIndicatorDetails details : dbIndicatorDetailsList){

                    String uuid = (String) details.getUuid();
                    if (uid.equals(uuid)){
                        indicatorDetails = details;
                        dbIndicatorDetailsList.remove(details);
                        break;
                    }

                }

                if (indicatorDetails != null){

                    if (indicatorName != null) indicatorDetails.setIndicatorName(indicatorName);
                    if (indicatorCode != null) indicatorDetails.setIndicatorCode(indicatorCode);
                    if (dataType != null) indicatorDetails.setDate(dataType);
                    if (topic != null) indicatorDetails.setTopic(topic);
                    if (definition != null) indicatorDetails.setDefinition(definition);
                    if (purposeAndIssues != null) indicatorDetails.setPurposeAndIssues(purposeAndIssues);
                    if (preferredDataSources != null) indicatorDetails.setPreferredDataSources(preferredDataSources);
                    if (methodOfEstimation != null) indicatorDetails.setMethodOfEstimation(methodOfEstimation);
                    if (proposedScoring != null) indicatorDetails.setProposedScoring(proposedScoring);
                    if (expectedFrequencyDataDissemination != null) indicatorDetails.setExpectedFrequencyDataDissemination(expectedFrequencyDataDissemination);
                    if (indicatorReference != null) indicatorDetails.setIndicatorReference(indicatorReference);

                    String publishedBaseUrl = AppConstants.DATA_STORE_ENDPOINT;
                    int publishedVersionNo = getVersions(publishedBaseUrl);

                    //Get metadata json
                    DbMetadataValue dbMetadataValue =  getMetadata(
                            publishedBaseUrl+publishedVersionNo);
                    if (dbMetadataValue == null){
                        return new Results(400, "There was an issue getting the published version.");
                    }

                    DbMetadataJsonData dbMetadataJsonData = dbMetadataValue.getMetadata();

                    List<DbIndicatorDetails> detailsList = new ArrayList<>();
//                    dbIndicatorDetailsList.add(indicatorDetails);
//                    detailsList.addAll(dbIndicatorDetailsList);

                    dbMetadataJsonData.setIndicatorDetails(detailsList);

                    dbMetadataValue.setMetadata(dbMetadataJsonData);

                    var response = GenericWebclient.putForSingleObjResponse(
                            publishedBaseUrl+publishedVersionNo,
                            dbMetadataValue,
                            DbMetadataValue.class,
                            Response.class);
                    if (response.getHttpStatusCode() == 200){
                        return new Results(200, new DbDetails("The indicators values have been updated."));
                    }
                    return new Results(400, "There was an issue adding the resource");


                }

            }





        }catch (Exception e){
            e.printStackTrace();
        }

        return new Results(400, "There was an issue processing the request. Please try again.");
    }

    @Override
    public Results deleteDictionary(String uid) {

        try{

            String publishedBaseUrl = AppConstants.DATA_STORE_ENDPOINT;
            int publishedVersionNo = getVersions(publishedBaseUrl);

            //Get metadata json
            DbMetadataValue dbMetadataValue =  getMetadata(
                    publishedBaseUrl+publishedVersionNo);
            if (dbMetadataValue == null){
                return new Results(400, "There was an issue getting the published version.");
            }
            DbMetadataJsonData dbMetadataJsonData = dbMetadataValue.getMetadata();
            List<DbIndicatorDetails> dbIndicatorDetailsList = dbMetadataJsonData.getIndicatorDetails();

            if (dbIndicatorDetailsList != null){

                for (DbIndicatorDetails dbIndicatorDetails : dbIndicatorDetailsList){
                    String uuid = (String) dbIndicatorDetails.getUuid();
                    if (uid.equals(uuid)){
                        dbIndicatorDetailsList.remove(dbIndicatorDetails);
                        break;
                    }
                }
                dbMetadataJsonData.setIndicatorDetails(dbIndicatorDetailsList);

                dbMetadataValue.setMetadata(dbMetadataJsonData);

                var response = GenericWebclient.putForSingleObjResponse(
                        publishedBaseUrl+publishedVersionNo,
                        dbMetadataValue,
                        DbMetadataValue.class,
                        Response.class);
                if (response.getHttpStatusCode() == 200){
                    return new Results(200, new DbDetails("The indicator has been deleted."));
                }
                return new Results(400, "There was an issue adding the resource");



            }




        }catch (Exception e){
            e.printStackTrace();
        }



        return null;
    }

    @Override
    public Results getTopics() {

        String[] myTopics = {
                "Selection",
                "Procurement",
                "Distribution",
                "Use",
                "Coordination and leadership",
                "Pharmaceutical Laws and Regulations",
                "Ethics, Transparency, and Accountability",
                "Inspection and Enforcement",
                "Product Assessment and Registration",
                "Quality and Safety Surveillance",
                "Innovation, Research & Development",
                "Intellectual Property & Trade",
                "Costing & Pricing",
                "Financial Risk Protection",
                "Expenditure Tracking & Monitoring",
                "Human Resource Development ",
                "Human Resource Management",
                "Information Policy and Data Standardization"
        };
        List<String> topicList = new ArrayList<>(Arrays.asList(myTopics));
        DbResults dbResults1 = new DbResults(topicList.size(), topicList);

        String[] dropDowns = {
                IndicatorDropDowns.TEXT.name(),
                IndicatorDropDowns.SELECTION.name(),
                IndicatorDropDowns.NUMBER.name(),
        };
        List<String> dropList = new ArrayList<>(Arrays.asList(dropDowns));
        DbResults dbResults2 = new DbResults(dropList.size(), dropList);


        DbIndicatorTypes dbIndicatorTypes = new DbIndicatorTypes(dbResults1, dbResults2);
        return new Results(200, dbIndicatorTypes);
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
    private DbMetadataValue getMetadata(String publishedBaseUrl){

        try{

            DbMetadataValue dbMetadataValue = GenericWebclient.getForSingleObjResponse(
                    publishedBaseUrl, DbMetadataValue.class);
            if (dbMetadataValue != null){
                return dbMetadataValue;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
