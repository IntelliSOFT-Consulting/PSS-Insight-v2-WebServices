package com.intellisoft.nationalinstance.service_impl;

import com.intellisoft.nationalinstance.*;
import com.intellisoft.nationalinstance.db.RespondentAnswers;
import com.intellisoft.nationalinstance.db.SurveyResendRequests;
import com.intellisoft.nationalinstance.db.SurveyRespondents;
import com.intellisoft.nationalinstance.db.Surveys;
import com.intellisoft.nationalinstance.db.repso.IndicatorsRepo;
import com.intellisoft.nationalinstance.db.repso.RespondentAnswersRepo;
import com.intellisoft.nationalinstance.db.repso.SurveyResendRequestsRepo;
import com.intellisoft.nationalinstance.db.repso.SurveyRespondentsRepo;
import com.intellisoft.nationalinstance.exception.CustomException;
import com.intellisoft.nationalinstance.model.Response;
import com.intellisoft.nationalinstance.util.AppConstants;
import com.intellisoft.nationalinstance.util.EmailService;
//import com.intellisoft.nationalinstance.util.MailService;
import com.intellisoft.nationalinstance.util.GenericWebclient;
import kotlin.Triple;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.net.InetAddress;
import java.util.*;


@Service
@RequiredArgsConstructor
public class SurveyRespondentsServiceImpl implements SurveyRespondentsService{

    private final SurveyRespondentsRepo respondentsRepo;

    private final FormatterClass formatterClass = new FormatterClass();
    private final SurveysService surveysService;
    private final IndicatorsRepo indicatorsRepo;
    private final RespondentAnswersRepo respondentAnswersRepo;
    private final SurveyResendRequestsRepo surveyResendRequestsRepo;
    private final VersionServiceImpl versionService;
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    public TemplateEngine templateEngine;


    @Override
    public Results addSurveyRespondent(DbSurveyRespondent dbSurveyRespondent) {

        List<String> emailAddressList = dbSurveyRespondent.getEmailAddressList();
        String expiryDateTime = dbSurveyRespondent.getExpiryDateTime();
        String surveyId = dbSurveyRespondent.getSurveyId();
        String customAppUrl = dbSurveyRespondent.getCustomAppUrl();

        for (int i = 0; i < emailAddressList.size(); i++){

            String emailAddress = emailAddressList.get(i);
            sendMail(emailAddress, expiryDateTime, surveyId, customAppUrl);

        }


        return new Results(200, new DbDetails("Please wait as we send the mails."));
    }

    private void sendMail(String emailAddress,
                          String expiryDateTime,
                          String surveyId,
                          String customAppUrl ){

        String password = formatterClass.getOtp();

        SurveyRespondents surveyRespondents = new SurveyRespondents();
        surveyRespondents.setSurveyId(surveyId);
        surveyRespondents.setEmailAddress(emailAddress);

        surveyRespondents.setStatus(PublishStatus.SENT.name());
        surveyRespondents.setCustomUrl(customAppUrl);

        surveyRespondents.setPassword(password);
        surveyRespondents.setExpiryTime(expiryDateTime);

        Optional<SurveyRespondents> optionalSurveyRespondents =
                respondentsRepo.findByEmailAddressAndSurveyId(emailAddress, surveyId);

        if (optionalSurveyRespondents.isPresent()){
            SurveyRespondents surveyDbRespondents = optionalSurveyRespondents.get();
            surveyDbRespondents.setPassword(password);
            surveyDbRespondents.setExpiryTime(expiryDateTime);
            surveyRespondents = respondentsRepo.save(surveyDbRespondents);
        }else {
            surveyRespondents = respondentsRepo.save(surveyRespondents);
        }

        Long respondentId = surveyRespondents.getId();
        String loginUrl = customAppUrl + "?id="+respondentId;

        List<DbSurveyRespondentData> dbSurveyRespondentDataList = new ArrayList<>();
        DbSurveyRespondentData dbSurveyRespondentData = new DbSurveyRespondentData(emailAddress, expiryDateTime, loginUrl, password);
        dbSurveyRespondentDataList.add(dbSurveyRespondentData);
        DbRespondents dbRespondents = new DbRespondents(dbSurveyRespondentDataList);
        sendBackgroundEmail(dbRespondents);


    }
    @Async
    void sendBackgroundEmail(DbRespondents dbRespondents){

        try{
            String hostname = InetAddress.getLocalHost().getHostAddress();
            System.out.println("===1"+hostname);

//            String mailServerUrl = "http://"+hostname+":7007/"+"api/v1/mail-service/send-email";
            String mailServerUrl = "http://"+"172.104.91.99"+":7007/"+"api/v1/mail-service/send-email";
            System.out.println("===2"+mailServerUrl);

            var response = GenericWebclient.postForSingleObjResponse(
                    mailServerUrl,
                    dbRespondents,
                    DbRespondents.class,
                    Response.class);
            System.out.println("RESPONSE FROM REMOTE: {}"+response);
            if (response.getHttpStatusCode() < 200) {
                System.out.println(response);
            }


        }catch (Exception e){
            e.printStackTrace();
        }


    }



    @Override
    public Results listSurveyRespondent(String surveyId) {

        List<DbRespondentDetails> dbRespondentDetailsList = new ArrayList<>();
        List<SurveyRespondents> respondentsList =
                respondentsRepo.findAllBySurveyId(surveyId);

        for (int j = 0; j< respondentsList.size(); j++){

            String respondentId = String.valueOf(respondentsList.get(j).getId());
            String emailAddress = respondentsList.get(j).getEmailAddress();
            String createdAt = String.valueOf(respondentsList.get(j).getCreatedAt());

            DbRespondentDetails dbRespondentDetails = new DbRespondentDetails(
                    respondentId,
                    emailAddress,
                    createdAt);
            dbRespondentDetailsList.add(dbRespondentDetails);

        }

        DbResults dbResults = new DbResults(
                dbRespondentDetailsList.size(),
                dbRespondentDetailsList);

        return new Results(200, dbResults);
    }

    @Override
    public Results deleteSurveyRespondent(String surveyId) {

        List<SurveyRespondents> respondentsList =
                respondentsRepo.findAllBySurveyId(surveyId);
        if (!respondentsList.isEmpty()){
            respondentsRepo.deleteAllBySurveyId(surveyId);
            return new Results(200,
                    new DbDetails("Records associated with the record have been deleted."));
        }
        return new Results(400, "Could not find any records for that id.");
    }

    @Override
    public Results deleteRespondent(String id) {
        Long respondentId = Long.valueOf(id);
        Optional<SurveyRespondents> optionalSurveyRespondents =
                respondentsRepo.findById(respondentId);
        if (optionalSurveyRespondents.isPresent()){
            respondentsRepo.deleteById(respondentId);
            return new Results(200, new DbDetails("Respondent has been deleted successfully."));
        }
        return new Results(400, "Could not find any records for that id.");
    }

    @Override
    public Results verifyPassword(DbSurveyDetails dbSurveyDetails) {

        Long respondentId = Long.valueOf(dbSurveyDetails.getRespondentId());
        String password = dbSurveyDetails.getPassword();

        Optional<SurveyRespondents> optionalSurveyRespondents =
                respondentsRepo.findById(respondentId);
        if (optionalSurveyRespondents.isPresent()){
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();
            String passwordDb = surveyRespondents.getPassword();
            if (password.equals(passwordDb)){
                //Return questions

                return new Results(200, new DbDetails("Log in success."));
            }
        }

        return new Results(400, "Password authentication failed.");
    }

    @Override
    public Results getAssignedSurvey(String id) {

        List<DbFrontendCategoryIndicators> categoryIndicatorsList = getRespondentsQuestions(id);
        DbResults dbResults = new DbResults(
                categoryIndicatorsList.size(),
                categoryIndicatorsList);
        return new Results(200, dbResults);

    }

    private List<DbFrontendCategoryIndicators> getRespondentsQuestions(String id){

        List<DbFrontendIndicators> indicatorForFrontEnds = new LinkedList<>();

        Optional<SurveyRespondents> optionalSurveyRespondents =
                respondentsRepo.findById(Long.valueOf(id));

        if (optionalSurveyRespondents.isPresent()) {
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();
            String surveyId = surveyRespondents.getSurveyId();

            Surveys surveys  = surveysService.surveyDetailsInfo(surveyId);
            if (surveys != null){
                List<String> stringList = surveys.getIndicators();

                List<String> metaDataList = indicatorsRepo.findMetadataByIndicatorIds(stringList);

                try {

                    for(int j = 0; j < metaDataList.size(); j++){
                        String s = metaDataList.get(j);
                        JSONObject jsonObject = new JSONObject(s);
                        versionService.getIndicatorGroupings(indicatorForFrontEnds, jsonObject);
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

                return categoryIndicatorsList;
            }
        }
        return new ArrayList<>();

    }

    @Override
    public Results getAssignedAnswers(String respondentId) {

        List<DbFrontendCategoryIndicatorsAnswers> categoryIndicatorsAnswersList = new ArrayList<>();
        List<DbFrontendCategoryIndicators> categoryIndicatorsList = getRespondentsQuestions(respondentId);
        for (int i = 0; i < categoryIndicatorsList.size(); i++){

            String catName = categoryIndicatorsList.get(i).getCategoryName();
            List<DbFrontendIndicatorAnswers> dbFrontendIndicatorAnswersList = new ArrayList<>();

            List<DbFrontendIndicators> indicatorlistData = categoryIndicatorsList.get(i).getIndicators();
            for (int j = 0; j < indicatorlistData.size(); j++){

                String code = indicatorlistData.get(j).getCode();
                String indicatorCategoryId = indicatorlistData.get(j).getIndicatorId();
                String categoryName = indicatorlistData.get(j).getCategoryName();
                String indicatorName = indicatorlistData.get(j).getIndicatorName();
                List<DbRespondentSurvey> dbRespondentSurveyList = new ArrayList<>();

                List<DbIndicators> indicatorList = indicatorlistData.get(j).getIndicators();
                for (int k = 0; k < indicatorList.size(); k++){

                    String indicatorId = indicatorList.get(k).getId();
                    Optional<RespondentAnswers> respondentAnswers = respondentAnswersRepo
                            .findByIndicatorIdAndRespondentId(indicatorId, respondentId);
                    if (respondentAnswers.isPresent()){

                        RespondentAnswers respondentAnswersValue = respondentAnswers.get();
                        String answer = String.valueOf(respondentAnswersValue.getAnswer());
                        String comment = respondentAnswersValue.getComments();
                        String upload = respondentAnswersValue.getAttachment();

                        DbRespondentSurvey dbRespondentSurvey = new DbRespondentSurvey(
                                indicatorId, answer, comment, upload);
                        dbRespondentSurveyList.add(dbRespondentSurvey);
                    }

                }

                DbFrontendIndicatorAnswers dbFrontendIndicatorAnswers = new DbFrontendIndicatorAnswers(
                        code, indicatorCategoryId, categoryName, indicatorName, dbRespondentSurveyList);
                dbFrontendIndicatorAnswersList.add(dbFrontendIndicatorAnswers);
            }

            DbFrontendCategoryIndicatorsAnswers dbFrontendCategoryIndicatorsAnswers = new DbFrontendCategoryIndicatorsAnswers(
                    catName, dbFrontendIndicatorAnswersList
            );
            categoryIndicatorsAnswersList.add(dbFrontendCategoryIndicatorsAnswers);
        }

        DbResults dbResults = new DbResults(
                categoryIndicatorsAnswersList.size(),
                categoryIndicatorsAnswersList);
        return new Results(200, dbResults);


    }



    @Override
    public Results saveResponse(DbResponse dbResponse) {

        List<RespondentAnswers> respondentAnswersList = new ArrayList<>();
        String respondentId = dbResponse.getRespondentId();
        List<DbRespondentSurvey> dbRespondentSurveyList = dbResponse.getResponses();
        for(int i = 0; i < dbRespondentSurveyList.size(); i++){
            String indicatorId = dbRespondentSurveyList.get(i).getIndicatorId();
            String answer = dbRespondentSurveyList.get(i).getAnswer();
            String comments = dbRespondentSurveyList.get(i).getComments();
            String attachment = dbRespondentSurveyList.get(i).getAttachment();
            RespondentAnswers respondentAnswers = new RespondentAnswers(
                    respondentId, indicatorId, answer, comments, attachment);
            respondentAnswersList.add(respondentAnswers);
        }
        //Update status on what was provided
        Optional<SurveyRespondents> optionalSurveyRespondents = respondentsRepo.findById(Long.valueOf(respondentId));
        if (optionalSurveyRespondents.isPresent()){
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();
            surveyRespondents.setStatus(PublishStatus.PENDING.name());
            respondentsRepo.save(surveyRespondents);
        }


        respondentAnswersRepo.saveAll(respondentAnswersList);
        return new Results(201, new DbDetails("Responses have been saved."));
    }

    @Override
    public Results requestLink(DbRequestLink dbRequestLink) {

        String respondentId = dbRequestLink.getRespondentId();
        String comment = dbRequestLink.getComments();

        SurveyResendRequests surveyResendRequests = new SurveyResendRequests();
        surveyResendRequests.setRespondentId(respondentId);
        surveyResendRequests.setComment(comment);
        surveyResendRequests.setStatus(PublishStatus.REQUESTED.name());

        surveyResendRequestsRepo.save(surveyResendRequests);
        resendEmail(respondentId);

        return new Results(200, new DbDetails("Request has been sent."));
    }

    public Results resendEmail(String respondentId){
        //Get respondent details
        Optional<SurveyRespondents> optionalSurveyRespondents =
                respondentsRepo.findById(Long.valueOf(respondentId));
        if (optionalSurveyRespondents.isPresent()){
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();
            String emailAddress = surveyRespondents.getEmailAddress();
            String customAppUrl = surveyRespondents.getCustomUrl();
            String expiryDateTime = surveyRespondents.getExpiryTime();
            String surveyId = surveyRespondents.getSurveyId();

            /**
             * Check expiry time
             */
            Triple<Long, Long, Long> tripleData = formatterClass.getRemainingTime(expiryDateTime);
            Long days = tripleData.getFirst();
            if (days > 1){
                String password = formatterClass.getOtp();

                surveyRespondents.setEmailAddress(emailAddress);
                surveyRespondents.setPassword(password);
                surveyRespondents.setStatus(PublishStatus.ACCEPTED.name());
                surveyRespondents.setExpiryTime(expiryDateTime);
                surveyRespondents.setCustomUrl(customAppUrl);

                respondentsRepo.save(surveyRespondents);
                sendMail(emailAddress, expiryDateTime, surveyId, customAppUrl);

                return new Results(200, new DbDetails("Email sent."));
            }else {
                return new Results(400,"The expiry date has reached.");
            }

        }

        return new Results(400, "The email could not be sent.");
    }
}
