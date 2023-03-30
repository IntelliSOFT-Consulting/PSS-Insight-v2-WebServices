package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.db.RespondentAnswers;
import com.intellisoft.pssnationalinstance.db.SurveyRespondents;
import com.intellisoft.pssnationalinstance.db.Surveys;
import com.intellisoft.pssnationalinstance.repository.RespondentAnswersRepository;
import com.intellisoft.pssnationalinstance.repository.SurveyRespondentsRepo;
import com.intellisoft.pssnationalinstance.repository.SurveysRepo;
import com.intellisoft.pssnationalinstance.service_impl.service.DataEntryService;
import com.intellisoft.pssnationalinstance.service_impl.service.JavaMailSenderService;
import com.intellisoft.pssnationalinstance.service_impl.service.NationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.SurveyRespondentsService;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SurveyRespondentsServiceImpl implements SurveyRespondentsService {

    private final SurveyRespondentsRepo respondentsRepo;
    private final FormatterClass formatterClass = new FormatterClass();

    private final SurveysRepo surveysRepo;
    private final NationalTemplateService nationalTemplateService;
    private final RespondentAnswersRepository respondentAnswersRepository;

    private final DataEntryService dataEntryService;

    private final JavaMailSenderService javaMailSenderService;

    @Override
    public Results addSurveyRespondent(DbSurveyRespondent dbSurveyRespondent) {
        List<String> emailAddressList = dbSurveyRespondent.getEmailAddressList();
        String expiryDateTime = dbSurveyRespondent.getExpiryDateTime();
        String surveyId = dbSurveyRespondent.getSurveyId();
        String customAppUrl = dbSurveyRespondent.getCustomAppUrl();

        boolean isDateValid = formatterClass.isDateFormatValid(expiryDateTime);
        if (!isDateValid){
            return new Results(400, "Change the date to yyyy-MM-dd HH:mm:ss.");
        }

        boolean isExpired = formatterClass.isPastToday(expiryDateTime);
        if (isExpired){
            return new Results(400, "This link is expired.");
        }

        String emailErrors = "";
        for (int i = 0; i < emailAddressList.size(); i++){

            String emailAddress = emailAddressList.get(i);

            boolean isEmailValid = formatterClass.isEmailValid(emailAddress);
            if (isEmailValid){
                sendMail(emailAddress, expiryDateTime, surveyId, customAppUrl);
            }else {
                emailErrors = emailErrors + emailAddress + ",";
            }
        }
        String emailResponse = "";
        if (!emailErrors.equals("")){
            emailResponse = "The following email addresses have an issue: " + emailErrors;
        }

        return new Results(200, new DbDetails("Please wait as we send the mails." + emailResponse));
    }

    private void sendMail(String emailAddress,
                          String expiryDateTime,
                          String surveyId,
                          String customAppUrl ){

        String password = formatterClass.getOtp();

        SurveyRespondents surveyRespondents = new SurveyRespondents();
        surveyRespondents.setSurveyId(surveyId);
        surveyRespondents.setEmailAddress(emailAddress);

        surveyRespondents.setSubmissionStatus(SurveyStatus.SENT.name());
        surveyRespondents.setRespondentsStatus(SurveySubmissionStatus.DRAFT.name());
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
        DbSurveyRespondentData dbSurveyRespondentData = new DbSurveyRespondentData(
                emailAddress, expiryDateTime, loginUrl, password);
        dbSurveyRespondentDataList.add(dbSurveyRespondentData);
        DbRespondents dbRespondents = new DbRespondents(dbSurveyRespondentDataList);
        sendBackgroundEmail(dbRespondents, MailStatus.SEND.name());

    }
    @Async
    void sendBackgroundEmail(DbRespondents dbRespondents, String status){

        try{

            javaMailSenderService.sendEmailBackground(dbRespondents, status);

//            String hostname = InetAddress.getLocalHost().getHostAddress();
//            System.out.println("===1"+hostname);
//
////            String mailServerUrl = "http://"+hostname+":7007/"+"api/v1/mail-service/send-email";
//            String mailServerUrl = "http://"+"172.104.91.99"+":7007/"+"api/v1/mail-service/send-email";
//            System.out.println("===2"+mailServerUrl);
//
//            var response = GenericWebclient.postForSingleObjResponse(
//                    mailServerUrl,
//                    dbRespondents,
//                    DbRespondents.class,
//                    String.class);
//            System.out.println("RESPONSE FROM REMOTE: {}"+response);



        }catch (Exception e){
            e.printStackTrace();
        }


    }


    @Override
    public Results listSurveyRespondent(String surveyId, String status) {





        return null;
    }

    @Override
    public List<SurveyRespondents> getSurveyRespondents(String surveyId, String status){

        List<SurveyRespondents> surveyRespondentsList = new ArrayList<>();
        if (status.equals(SurveySubmissionStatus.EXPIRED.name())){
            surveyRespondentsList = respondentsRepo.findAllBySurveyId(surveyId);
        }else {
            surveyRespondentsList = respondentsRepo.findBySurveyIdAndRespondentsStatus(surveyId, status);
        }
        return surveyRespondentsList;
    }

    @Override
    public Results deleteSurveyRespondent(String surveyId) {
        return null;
    }

    @Override
    public Results deleteRespondent(String id) {
        return null;
    }

    @Override
    public Results verifyPassword(DbVerifySurvey dbVerifySurvey) {
        Long respondentId = Long.valueOf(dbVerifySurvey.getRespondentId());
        String password = dbVerifySurvey.getPassword();

        Optional<SurveyRespondents> optionalSurveyRespondents =
                respondentsRepo.findById(respondentId);
        if (optionalSurveyRespondents.isPresent()){
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();
            String expiryDate = surveyRespondents.getExpiryTime();

            boolean isExpired = formatterClass.isPastToday(expiryDate);
            if (isExpired){
                return new Results(400, "This link is expired.");
            }

            String passwordDb = surveyRespondents.getPassword();
            if (password.equals(passwordDb)){
                return new Results(200, new DbDetails("Verification success."));
            }
        }

        return new Results(400, "Password authentication failed.");
    }

    @Override
    public Results getAssignedSurvey(String respondentId) {

        Optional<SurveyRespondents> optionalSurveyRespondents =
                respondentsRepo.findById(Long.valueOf(respondentId));
        if (optionalSurveyRespondents.isPresent()){
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();

            String expiryDate = surveyRespondents.getExpiryTime();
            boolean isExpired = formatterClass.isPastToday(expiryDate);
            if (isExpired){
                return new Results(400, "This link is expired.");
            }

            String surveyId = surveyRespondents.getSurveyId();
            DbResponseDetails dbPublishedVersion = getRespondentsQuestions(surveyId,
                    respondentId);


            return new Results(200, dbPublishedVersion);
        }

        return new Results(400, "Cannot find questions");

    }

    @Override
    public Results saveResponse(DbResponse dbResponse) {
        List<RespondentAnswers> respondentAnswersList = new ArrayList<>();
        String respondentId = dbResponse.getRespondentId();
        boolean isSubmit = Boolean.TRUE.equals(dbResponse.isSubmit());
        String status = SurveySubmissionStatus.PENDING.name();
        if (!isSubmit){
            status = SurveySubmissionStatus.DRAFT.name();
        }

        List<DbRespondentSurvey> dbRespondentSurveyList = dbResponse.getResponses();
        for(int i = 0; i < dbRespondentSurveyList.size(); i++){
            String indicatorId = dbRespondentSurveyList.get(i).getIndicatorId();
            String answer = dbRespondentSurveyList.get(i).getAnswer();
            String comments = dbRespondentSurveyList.get(i).getComments();
            String attachment = dbRespondentSurveyList.get(i).getAttachment();
            RespondentAnswers respondentAnswers = new RespondentAnswers(
                    respondentId, indicatorId, answer, comments, attachment);
            respondentAnswers.setStatus(SurveyRespondentStatus.PENDING.name());
            respondentAnswersList.add(respondentAnswers);
        }
        //Update status on what was provided
        Optional<SurveyRespondents> optionalSurveyRespondents =
                respondentsRepo.findById(Long.valueOf(respondentId));
        if (optionalSurveyRespondents.isPresent()){
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();

            String expiryDate = surveyRespondents.getExpiryTime();
            boolean isExpired = formatterClass.isPastToday(expiryDate);
            if (isExpired){
                return new Results(400, "This link is expired.");
            }

            surveyRespondents.setSubmissionStatus(status);
            respondentsRepo.save(surveyRespondents);
        }


        respondentAnswersRepository.saveAll(respondentAnswersList);
        return new Results(201, new DbDetails("Responses have been saved."));
    }

    @Override
    public Results getRespondentDetails(
            String respondentId,
            String questions,
            String responses,
            String respondentDetails) {

        Optional<SurveyRespondents> optionalSurveyRespondents =
                respondentsRepo.findById(Long.valueOf(respondentId));
        if (optionalSurveyRespondents.isPresent()){
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();

            String emailAddress = surveyRespondents.getEmailAddress();
            String expiryTime = surveyRespondents.getExpiryTime();
            String surveyId = surveyRespondents.getSurveyId();
            String status = surveyRespondents.getRespondentsStatus();

            String landingPage = "";
            String surveyName = "";
            String surveyDesc = "";

            Optional<Surveys> optionalSurvey = surveysRepo.findById(Long.valueOf(surveyId));
            if (optionalSurvey.isPresent()){
                Surveys surveys = optionalSurvey.get();
                landingPage = surveys.getLandingPage();
                surveyName = surveys.getName();
                surveyDesc = surveys.getDescription();

            }

            String expiryDate = surveyRespondents.getExpiryTime();

            boolean isExpired = formatterClass.isPastToday(expiryDate);
//            if (isExpired){
//                return new Results(400, "This link is expired.");
//            }

            DbResponseDetails dbResponseDetailsValues =
                    new DbResponseDetails(
                            null,
                            null,
                            null);

            if (respondentDetails != null){
                DbRespondentsDetails dbRespondentsDetails =
                        new DbRespondentsDetails(
                                Long.parseLong(respondentId),
                                emailAddress,
                                expiryTime,
                                status,
                                surveyName,
                                surveyDesc,
                                landingPage,
                                null
                        );
                dbResponseDetailsValues.setRespondentDetails(dbRespondentsDetails);
            }
            if (responses != null){
                DbResponseDetails dbResponseDetails =
                        getRespondentsQuestions(surveyId, respondentId);
                if (dbResponseDetails != null){
                    dbResponseDetailsValues.setResponses(
                            dbResponseDetails.getResponses()
                    );
                }

            }
            if (questions != null){
                DbResponseDetails dbResponseDetails =
                        getRespondentsQuestions(surveyId, respondentId);
                if (dbResponseDetails != null){
                    dbResponseDetailsValues.setQuestions(
                            dbResponseDetails.getQuestions()
                    );
                }
            }

            return new Results(200, dbResponseDetailsValues);

        }


        return new Results(400, "We could not find the user");
    }

    @Override
    public Results resendSurvey(
            String respondentId, DbResendSurvey dbResendSurvey) {

        Optional<SurveyRespondents> optionalSurveyRespondents =
                respondentsRepo.findById(Long.valueOf(respondentId));
        if (optionalSurveyRespondents.isPresent()){
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();
            String surveyId = surveyRespondents.getSurveyId();



            String emailAddress = surveyRespondents.getEmailAddress();
            String loginUrl = surveyRespondents.getCustomUrl();
            String password = surveyRespondents.getPassword();

            String expiryDateTime = dbResendSurvey.getExpiryDateTime();

            List<DbSurveyRespondentData> dbSurveyRespondentDataList = new ArrayList<>();
            DbSurveyRespondentData dbSurveyRespondentData = new DbSurveyRespondentData(
                    emailAddress, expiryDateTime, loginUrl, password);
            dbSurveyRespondentDataList.add(dbSurveyRespondentData);

            boolean isDateValid = formatterClass.isDateFormatValid(expiryDateTime);
            if (!isDateValid){
                return new Results(400, "Change the date to yyyy-MM-dd HH:mm:ss.");
            }

            boolean isExpired = formatterClass.isPastToday(expiryDateTime);
            if (isExpired){
                return new Results(400, "This link is expired.");
            }

            DbRespondents dbRespondents = new DbRespondents(dbSurveyRespondentDataList);
            sendBackgroundEmail(dbRespondents, MailStatus.RESEND.name());

            return new Results(200, new DbDetails("We have sent the email."));

        }

        return new Results(400, "There was an issue with this request.");

    }

    @Override
    public Results confirmSurvey(String respondentId, DbConfirmSurvey dbConfirmSurvey) {

        String orgUnit = dbConfirmSurvey.getOrgUnit();
        String selectedPeriod = dbConfirmSurvey.getSelectedPeriod();
        String dataEntryPersonId = dbConfirmSurvey.getDataEntryPersonId();

        List<DbDataEntryResponses> dataEntryResponsesList = new ArrayList<>();

        List<RespondentAnswers> respondentAnswersList =
                respondentAnswersRepository.findByRespondentIdAndStatus(
                        respondentId, SurveyRespondentStatus.PENDING.name());
        for (RespondentAnswers respondentAnswers : respondentAnswersList){

            String indicatorId = respondentAnswers.getIndicatorId();
            String answer = respondentAnswers.getAnswer();
            String comments = respondentAnswers.getComments();
            String attachment = respondentAnswers.getAttachment();

            DbDataEntryResponses dbDataEntryResponses = new DbDataEntryResponses(
                    indicatorId,
                    answer,
                    comments,
                    attachment);
            dataEntryResponsesList.add(dbDataEntryResponses);

        }

        DbDataEntryData dbDataEntryData = new DbDataEntryData(
                orgUnit,
                selectedPeriod,
                true,
                dataEntryPersonId,
                null,
                dataEntryResponsesList);

        dataEntryService.saveEventData(dbDataEntryData);

        return new Results(200, new DbDetails("We are processing the request."));
    }

    @Override
    public Results requestLink(String respondentId, DbRequestLink dbRequestLink) {

        String resendComment = dbRequestLink.getComment();

        Optional<SurveyRespondents> optionalSurveyRespondents =
                respondentsRepo.findById(Long.valueOf(respondentId));
        if (optionalSurveyRespondents.isPresent()){
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();
            surveyRespondents.setRespondentsStatus(SurveyRespondentStatus.RESEND_REQUEST.name());
            respondentsRepo.save(surveyRespondents);

            return new Results(200, new DbDetails("Request has been sent."));
        }

        return new Results(400, "There was an issue processing the request.");
    }

    @Override
    public List<SurveyRespondents> getSurveyRespondents() {
        return respondentsRepo.findBySubmissionStatus(SurveyStatus.SENT.name());
    }

    private DbResponseDetails getRespondentsQuestions(String surveyId, String respondentId){

        List<String> indicatorList = new ArrayList<>();
        Optional<Surveys> optionalSurvey = surveysRepo.findById(Long.valueOf(surveyId));
        if (optionalSurvey.isPresent()){
            Surveys surveys = optionalSurvey.get();
            indicatorList = surveys.getIndicators();

            for (String indicator : indicatorList){

                Optional<RespondentAnswers> optionalRespondentAnswers =
                        respondentAnswersRepository.findById(Long.valueOf(indicator));
                if (optionalRespondentAnswers.isPresent()){
                    String status = optionalRespondentAnswers.get().getStatus();
                    if (status.equals(SurveyRespondentStatus.VERIFIED.name())){
                        indicatorList.remove(indicator);
                    }
                }

            }

        }

        DbPublishedVersion dbPublishedVersion =
                nationalTemplateService.nationalPublishedIndicators();
        if (dbPublishedVersion != null){

            List<DbIndicators> newDbIndicatorsList = new ArrayList<>();
            List<DbDataEntryResponses> dataEntryResponsesList = new ArrayList<>();

            List<DbIndicators> dbIndicatorsList = dbPublishedVersion.getDetails();
            for (DbIndicators dbIndicators : dbIndicatorsList){

                String categoryName = (String) dbIndicators.getCategoryName();
                List<DbIndicatorValues> newIndicatorList = new ArrayList<>();

                List<DbIndicatorValues> dbIndicatorValuesList = dbIndicators.getIndicators();
                for (DbIndicatorValues dbIndicatorValues : dbIndicatorValuesList){

                    String categoryId = (String) dbIndicatorValues.getCategoryId();
                    if (indicatorList.contains(categoryId)){
                        newIndicatorList.add(dbIndicatorValues);
                        List<DbIndicatorDataValues> dataValuesList =
                                dbIndicatorValues.getIndicatorDataValue();

                        for (DbIndicatorDataValues dbIndicatorDataValues: dataValuesList){
                            String indicatorId = (String) dbIndicatorDataValues.getId();
                            //get responses
                            Optional<RespondentAnswers> answersOptional = respondentAnswersRepository
                                    .findByIndicatorIdAndRespondentId(indicatorId, respondentId);
                            if (answersOptional.isPresent()){
                                RespondentAnswers respondentAnswers = answersOptional.get();
                                String answer = respondentAnswers.getAnswer();
                                String comments = respondentAnswers.getComments();
                                String attachment = respondentAnswers.getAttachment();

                                DbDataEntryResponses dbIndicatorDataResponses =
                                        new DbDataEntryResponses(
                                                indicatorId,
                                                answer,
                                                comments,
                                                attachment);
                                dataEntryResponsesList.add(dbIndicatorDataResponses);

                            }

                        }

                    }

                }

                DbIndicators newDbIndicators = new DbIndicators(
                        categoryName, newIndicatorList);
                newDbIndicatorsList.add(newDbIndicators);

            }

            return new DbResponseDetails(
                    newDbIndicatorsList,
                    dataEntryResponsesList, null);
        }
        return null;
    }


}
