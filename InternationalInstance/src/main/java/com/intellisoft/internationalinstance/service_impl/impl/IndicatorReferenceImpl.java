package com.intellisoft.internationalinstance.service_impl.impl;

import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.model.Response;
import com.intellisoft.internationalinstance.service_impl.service.IndicatorReferenceService;
import com.intellisoft.internationalinstance.util.AppConstants;
import com.intellisoft.internationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class IndicatorReferenceImpl implements IndicatorReferenceService {

    private final FormatterClass formatterClass = new FormatterClass();

    @Override
    public Results addIndicatorDictionary(DbIndicatorDetails dbIndicatorDetails) {

        /**
         * TODO: Check on how to create indicators into the datastore,
         Remember to add comments and uploads
         */


        try{

            String uuid = formatterClass.getUUid();
            dbIndicatorDetails.setUuid(uuid);
            String publishedBaseUrl = AppConstants.DATA_STORE_ENDPOINT;
            int publishedVersionNo = getVersions(publishedBaseUrl);

            //Get metadata json
            DbMetadataValue dbMetadataValue =  getMetadata(publishedBaseUrl+publishedVersionNo);
            if (dbMetadataValue == null){
                return new Results(400, "There was an issue getting the published version.");
            }

            dbIndicatorDetails.setDate(formatterClass.getTodayDate());

            DbMetadataJsonData dbMetadataJsonData = dbMetadataValue.getMetadata();

            List<DbIndicatorDetails> detailsList = new ArrayList<>();
            List<DbIndicatorDetails> dbIndicatorDetailsList = dbMetadataJsonData.getIndicatorDetails();
            if (dbIndicatorDetailsList != null){
                dbIndicatorDetailsList.add(dbIndicatorDetails);
                detailsList.addAll(dbIndicatorDetailsList);
            }else {
                detailsList.add(dbIndicatorDetails);
            }


            dbMetadataJsonData.setIndicatorDetails(detailsList);

            dbMetadataValue.setMetadata(dbMetadataJsonData);

            var response = GenericWebclient.putForSingleObjResponse(
                    publishedBaseUrl+publishedVersionNo,
                    dbMetadataValue,
                    DbMetadataValue.class,
                    Response.class);

            if (response.getHttpStatusCode() == 200){
                return new Results(200, new DbDetails("The indicators values have been added."));
            }
            return new Results(400, "There was an issue adding the resource");

        }catch (Exception e){
            e.printStackTrace();
            return new Results(400, "Please try again after some time");

        }
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
            String publishedBaseUrl = AppConstants.DATA_STORE_ENDPOINT;
            int publishedVersionNo = getVersions(publishedBaseUrl);
            //Get metadata json
            DbMetadataValue dbMetadataValue =  getMetadata(publishedBaseUrl+publishedVersionNo);
            if (dbMetadataValue != null){
                return dbMetadataValue.getMetadata().getIndicatorDetails();
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
                    dbIndicatorDetailsList.add(indicatorDetails);
                    detailsList.addAll(dbIndicatorDetailsList);

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
