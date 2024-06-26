package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.db.ResendIndicators;
import com.intellisoft.pssnationalinstance.db.RespondentAnswers;
import com.intellisoft.pssnationalinstance.db.SurveyRespondents;
import com.intellisoft.pssnationalinstance.db.Surveys;
import com.intellisoft.pssnationalinstance.repository.ResendIndicatorsRepository;
import com.intellisoft.pssnationalinstance.repository.RespondentAnswersRepository;
import com.intellisoft.pssnationalinstance.repository.SurveyRespondentsRepo;
import com.intellisoft.pssnationalinstance.repository.SurveysRepo;
import com.intellisoft.pssnationalinstance.service_impl.service.DataEntryService;
import com.intellisoft.pssnationalinstance.service_impl.service.JavaMailSenderService;
import com.intellisoft.pssnationalinstance.service_impl.service.NationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.SurveyRespondentsService;
import com.intellisoft.pssnationalinstance.util.EnvUrlConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class SurveyRespondentsServiceImpl implements SurveyRespondentsService {

    private final SurveyRespondentsRepo respondentsRepo;
    private final FormatterClass formatterClass = new FormatterClass();

    private final SurveysRepo surveysRepo;
    private final NationalTemplateService nationalTemplateService;
    private final RespondentAnswersRepository respondentAnswersRepository;

    private final DataEntryService dataEntryService;

    private final JavaMailSenderService javaMailSenderService;
    private final ResendIndicatorsRepository repository;
    private final EnvUrlConstants envUrlConstants;
    private final EnvConfig envConfig;

    private HttpEntity<String> getHeaders() {

        String username = envConfig.getValue().getUsername();
        String password = envConfig.getValue().getPassword();

        String auth = username + ":" + password;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64Utils.encodeToString(auth.getBytes()));

        return new HttpEntity<>(headers);

    }

    @Override
    public Results addSurveyRespondent(DbSurveyRespondent dbSurveyRespondent) {
        List<String> emailAddressList = dbSurveyRespondent.getEmailAddressList();
        String expiryDateTime = dbSurveyRespondent.getExpiryDateTime();
        String surveyId = dbSurveyRespondent.getSurveyId();
        String customAppUrl = dbSurveyRespondent.getCustomAppUrl();

        boolean isDateValid = formatterClass.isDateFormatValid(expiryDateTime);
        if (!isDateValid) {
            return new Results(400, "Change the date to yyyy-MM-dd HH:mm:ss.");
        }

        boolean isExpired = formatterClass.isPastToday(expiryDateTime);
        if (isExpired) {
            return new Results(400, "This link is expired.");
        }

        String emailErrors = "";
        for (int i = 0; i < emailAddressList.size(); i++) {

            String emailAddress = emailAddressList.get(i);

            boolean isEmailValid = formatterClass.isEmailValid(emailAddress);
            if (isEmailValid) {
                sendMail(emailAddress, expiryDateTime, surveyId, customAppUrl);
            } else {
                emailErrors = emailErrors + emailAddress + ",";
            }
        }
        String emailResponse = "";
        if (!emailErrors.equals("")) {
            emailResponse = "The following email addresses have an issue: " + emailErrors;
        }

        Optional<Surveys> optionalSurveys = surveysRepo.findById(Long.valueOf(surveyId));
        if (optionalSurveys.isPresent()) {
            Surveys surveys = optionalSurveys.get();
            surveys.setStatus(SurveyStatus.SENT.name());
            surveysRepo.save(surveys);
        }

        return new Results(200, new DbDetails("Please wait as we send the mails." + emailResponse));
    }

    private void sendMail(String emailAddress, String expiryDateTime, String surveyId, String customAppUrl) {

        String password = formatterClass.getOtp().trim();

        SurveyRespondents surveyRespondents = new SurveyRespondents();
        surveyRespondents.setSurveyId(surveyId);
        surveyRespondents.setEmailAddress(emailAddress);

        surveyRespondents.setSurveyStatus(SurveyStatus.SENT.name()); // Sent Survey
        surveyRespondents.setRespondentsStatus(SurveySubmissionStatus.DRAFT.name()); // Draft because they have not yet responded
        surveyRespondents.setCustomUrl(customAppUrl);

        surveyRespondents.setPassword(password);
        surveyRespondents.setExpiryTime(expiryDateTime);

        Optional<SurveyRespondents> optionalSurveyRespondents = respondentsRepo.findByEmailAddressAndSurveyId(emailAddress, surveyId);

        if (optionalSurveyRespondents.isPresent()) {
            SurveyRespondents surveyDbRespondents = optionalSurveyRespondents.get();
            surveyDbRespondents.setPassword(password);
            surveyDbRespondents.setExpiryTime(expiryDateTime);
            surveyRespondents = respondentsRepo.save(surveyDbRespondents);
        } else {
            surveyRespondents = respondentsRepo.save(surveyRespondents);
        }

        Long respondentId = surveyRespondents.getId();
        String loginUrl = customAppUrl + "?id=" + respondentId;

        List<DbSurveyRespondentData> dbSurveyRespondentDataList = new ArrayList<>();
        DbSurveyRespondentData dbSurveyRespondentData = new DbSurveyRespondentData(emailAddress, expiryDateTime, loginUrl, password);
        dbSurveyRespondentDataList.add(dbSurveyRespondentData);
        DbRespondents dbRespondents = new DbRespondents(dbSurveyRespondentDataList);
        sendBackgroundEmail(dbRespondents, MailStatus.SEND.name());

    }

    void sendBackgroundEmail(DbRespondents dbRespondents, String status) {

        try {

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


        } catch (Exception e) {
            log.error("Error occurred while sending email");
        }


    }


    @Override
    public Results listSurveyRespondent(String surveyId, String status) {


        return null;
    }

    @Override
    public List<SurveyRespondents> getSurveyRespondents(String surveyId, String status) {
        List<SurveyRespondents> surveyRespondentsList = new ArrayList<>();
        if (status.equals(SurveySubmissionStatus.EXPIRED.name())) {
            surveyRespondentsList = respondentsRepo.findAllBySurveyId(surveyId);
        } else if (status.equals("ALL")) {
            surveyRespondentsList = respondentsRepo.findAllBySurveyId(surveyId);
        } else {
            surveyRespondentsList = respondentsRepo.findBySurveyIdAndRespondentsStatus(surveyId, status); //bug-fix
        }
        return surveyRespondentsList;
    }

    @Override
    public Results deleteSurveyRespondent(String surveyId) {
        return null;
    }

    @Override
    public Results deleteRespondent(String respondentId) {

        Optional<SurveyRespondents> optionalSurveyRespondents = respondentsRepo.findById(Long.valueOf(respondentId));
        if (optionalSurveyRespondents.isPresent()) {
            respondentsRepo.deleteById(Long.valueOf(respondentId));
            return new Results(200, new DbDetails("Resource has been deleted successfully."));
        }

        return new Results(400, "Resource not found.");
    }

    @Override
    public Results verifyPassword(DbVerifySurvey dbVerifySurvey) {
        Long respondentId = Long.valueOf(dbVerifySurvey.getRespondentId());
        String password = dbVerifySurvey.getPassword();

        Optional<SurveyRespondents> optionalSurveyRespondents = respondentsRepo.findById(respondentId);
        if (optionalSurveyRespondents.isPresent()) {
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();
            String expiryDate = surveyRespondents.getExpiryTime();
            String respondentsStatus = surveyRespondents.getRespondentsStatus();

            boolean isExpired = formatterClass.isPastToday(expiryDate);
            if (isExpired) {
                return new Results(400, "This link is expired.");
            }

            String passwordDb = surveyRespondents.getPassword();
            if (password.equals(passwordDb)) {
                // check if the survey respondent had submitted the survey:
                if (respondentsStatus.equals(SurveySubmissionStatus.PENDING_CONFIRMATION)) {
                    return new Results(400, "You have already submitted the survey. Access denied");
                } else {
                    return new Results(200, new DbDetails("Verification success."));
                }
            }
        }

        return new Results(400, "Password authentication failed.");
    }

    @Override
    public Results getAssignedSurvey(String respondentId) throws URISyntaxException {


        Optional<SurveyRespondents> optionalSurveyRespondents = respondentsRepo.findById(Long.valueOf(respondentId));
        if (optionalSurveyRespondents.isPresent()) {
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();

            String expiryDate = surveyRespondents.getExpiryTime();
            boolean isExpired = formatterClass.isPastToday(expiryDate);
            if (isExpired) {
                return new Results(400, "This link is expired.");
            }

            String surveyId = surveyRespondents.getSurveyId();
            DbResponseDetails dbPublishedVersion = getRespondentsQuestions(surveyId, respondentId);


            return new Results(200, dbPublishedVersion);
        }

        return new Results(400, "Cannot find questions");

    }

    @Override
    public Results saveResponse(DbResponse dbResponse) {

        List<RespondentAnswers> respondentAnswersList = new ArrayList<>();
        String respondentId = dbResponse.getRespondentId();
        boolean isSubmit = dbResponse.isSubmit();
        String status;
        if (isSubmit) {
            status = SurveySubmissionStatus.PENDING.name();


            List<DbRespondentSurvey> dbRespondentSurveyList = dbResponse.getResponses();
            for (int i = 0; i < dbRespondentSurveyList.size(); i++) {

                LocalDate currentDateTime = LocalDate.now();

                String indicatorId = dbRespondentSurveyList.get(i).getIndicatorId();
                String answer = dbRespondentSurveyList.get(i).getAnswer();
                String comments = dbRespondentSurveyList.get(i).getComments();
                String attachment = dbRespondentSurveyList.get(i).getAttachment();
                RespondentAnswers respondentAnswers = new RespondentAnswers(respondentId, indicatorId, answer, comments, attachment);
                respondentAnswers.setStatus(status);
                respondentAnswersList.add(respondentAnswers);
            }
            //Update status on what was provided
            Optional<SurveyRespondents> optionalSurveyRespondents = respondentsRepo.findById(Long.valueOf(respondentId));
            if (optionalSurveyRespondents.isPresent()) {
                SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();

                String expiryDate = surveyRespondents.getExpiryTime();
                boolean isExpired = formatterClass.isPastToday(expiryDate);
                if (isExpired) {
                    return new Results(400, "This link is expired.");
                }

                surveyRespondents.setSurveyStatus(SurveyStatus.SENT.name()); //status of sent survey
                surveyRespondents.setRespondentsStatus(status); //status of survey_respondent
                respondentsRepo.save(surveyRespondents);
            }
        }


        respondentAnswersRepository.saveAll(respondentAnswersList);
        return new Results(201, new DbDetails("Responses have been saved."));
    }

    @Override
    public Results getRespondentDetails(String respondentId, String questions, String responses, String respondentDetails) throws URISyntaxException {

        Optional<SurveyRespondents> optionalSurveyRespondents = respondentsRepo.findById(Long.valueOf(respondentId));
        if (optionalSurveyRespondents.isPresent()) {
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

            String emailAddress = surveyRespondents.getEmailAddress();
            String expiryTime = surveyRespondents.getExpiryTime();
            String surveyId = surveyRespondents.getSurveyId();
            String status = surveyRespondents.getRespondentsStatus();
            String dateFilled = outputFormat.format(surveyRespondents.getCreatedAt());

            String landingPage = "";
            String surveyName = "";
            String surveyDesc = "";

            Optional<Surveys> optionalSurvey = surveysRepo.findById(Long.valueOf(surveyId));
            if (optionalSurvey.isPresent()) {
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

            DbResponseDetails dbResponseDetailsValues = new DbResponseDetails(null, null, null, null);

            if (respondentDetails != null) {

                DbMetadataJson publishedData = nationalTemplateService.getPublishedMetadataJson();
                DbPrograms dbPrograms = publishedData.getMetadata();
                //Get the reference sheet
                String refSheet = "";
                if (dbPrograms != null) {
                    Object referenceSheet = dbPrograms.getReferenceSheet();
                    if (referenceSheet != null) {
                        refSheet = (String) referenceSheet;
                    }
                }


                DbRespondentsDetails dbRespondentsDetails = new DbRespondentsDetails(Long.parseLong(respondentId), emailAddress, expiryTime, status, surveyName, surveyDesc, landingPage, refSheet, dateFilled);
                dbResponseDetailsValues.setRespondentDetails(dbRespondentsDetails);
            }
            if (responses != null) {
                DbResponseDetails dbResponseDetails = getRespondentsQuestions(surveyId, respondentId);
                if (dbResponseDetails != null) {
                    dbResponseDetailsValues.setResponses(dbResponseDetails.getResponses());
                }

            }

            if (questions != null) {
                Optional<ResendIndicators> optionalResendIndicators = repository.findBySurveyIdAndRespondentId(surveyId, respondentId);
                if (optionalResendIndicators.isPresent()) {
                    ResendIndicators resendIndicators = optionalResendIndicators.get();
                    List<String> stringList = resendIndicators.getResentIndicators();
                    dbResponseDetailsValues.setResentQuestions(stringList);
                }


//                System.out.println("dbResponseDetailsValues" +dbResponseDetailsValues);


                DbResponseDetails dbResponseDetails = getRespondentsQuestions(surveyId, respondentId);
                if (dbResponseDetails != null) {
                    dbResponseDetailsValues.setQuestions(dbResponseDetails.getQuestions());
                }
            }


            return new Results(200, dbResponseDetailsValues);

        }


        return new Results(400, "We could not find the user");
    }

    @Override
    public Results resendSurvey(String respondentId, DbResendSurvey dbResendSurvey) {

        // Works for resending expired / resending

        Optional<SurveyRespondents> optionalSurveyRespondents = respondentsRepo.findById(Long.valueOf(respondentId));
        if (optionalSurveyRespondents.isPresent()) {
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();
            String surveyId = surveyRespondents.getSurveyId();

            String emailAddress = surveyRespondents.getEmailAddress();
            String customAppUrl = surveyRespondents.getCustomUrl();
            String password = surveyRespondents.getPassword();
            String loginUrl = customAppUrl + "?id=" + respondentId;


            String expiryDateTime = dbResendSurvey.getExpiryDateTime();
            boolean isDateValid = formatterClass.isDateFormatValid(expiryDateTime);
            if (!isDateValid) {
                return new Results(400, "Change the date to yyyy-MM-dd HH:mm:ss.");
            }

            boolean isExpired = formatterClass.isPastToday(expiryDateTime);
            if (isExpired) {
                return new Results(400, "This date is expired.");
            }


            List<String> indicatorList = dbResendSurvey.getIndicators();
            String comments = dbResendSurvey.getComments();

            if (!indicatorList.isEmpty()) {

                //Remove these responses
//                for(String indicator: indicatorList){
//                    Optional<RespondentAnswers> optionalRespondentAnswers =
//                            respondentAnswersRepository.findByIndicatorIdAndRespondentId(indicator, respondentId);
//                    if (optionalRespondentAnswers.isPresent()){
//                        RespondentAnswers respondentAnswers = optionalRespondentAnswers.get();
//                        respondentAnswersRepository.deleteById(respondentAnswers.getId());
//                    }
//                }


                SurveyRespondents respondents = optionalSurveyRespondents.get();
                respondents.setExpiryTime(expiryDateTime);
                respondents.setSurveyStatus(SurveyStatus.SENT.name());
                respondents.setRespondentsStatus(SurveySubmissionStatus.PENDING_CONFIRMATION.name());
                respondentsRepo.save(respondents);

                ResendIndicators resendIndicators = new ResendIndicators();
                resendIndicators.setRespondentId(respondentId);
                resendIndicators.setResentIndicators(indicatorList);
                resendIndicators.setSurveyId(surveyId);

                Optional<ResendIndicators> optionalResendIndicators = repository.findBySurveyIdAndRespondentId(surveyId, respondentId);
                if (optionalResendIndicators.isPresent()) {
                    ResendIndicators resendIndicators1 = optionalResendIndicators.get();
                    resendIndicators1.setResentIndicators(indicatorList);
                    repository.save(resendIndicators1);
                } else {
                    repository.save(resendIndicators);
                }


            }

            List<DbSurveyRespondentData> dbSurveyRespondentDataList = new ArrayList<>();
            DbSurveyRespondentData dbSurveyRespondentData = new DbSurveyRespondentData(emailAddress, expiryDateTime, loginUrl, password);
            dbSurveyRespondentDataList.add(dbSurveyRespondentData);

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

        List<RespondentAnswers> respondentAnswersList = respondentAnswersRepository.findByRespondentIdAndStatus(respondentId, SurveyRespondentStatus.PENDING_CONFIRMATION.name());
        for (RespondentAnswers respondentAnswers : respondentAnswersList) {

            String indicatorId = respondentAnswers.getIndicatorId();
            String answer = respondentAnswers.getAnswer();
            String comments = respondentAnswers.getComments();
            String attachment = respondentAnswers.getAttachment();

            DbDataEntryResponses dbDataEntryResponses = new DbDataEntryResponses(indicatorId, answer, comments, attachment);
            dataEntryResponsesList.add(dbDataEntryResponses);

        }

        DbDataEntryData dbDataEntryData = new DbDataEntryData(null, orgUnit, selectedPeriod, true, dataEntryPersonId, null, null, null);

        dataEntryService.saveEventData(dbDataEntryData);

        return new Results(200, new DbDetails("We are processing the request."));
    }

    @Override
    public Results requestLink(String respondentId, DbRequestLink dbRequestLink) {

        String resendComment = dbRequestLink.getComment();

        Optional<SurveyRespondents> optionalSurveyRespondents = respondentsRepo.findById(Long.valueOf(respondentId));
        if (optionalSurveyRespondents.isPresent()) {
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();
            surveyRespondents.setRespondentsStatus(SurveyRespondentStatus.RESEND_REQUEST.name());
            respondentsRepo.save(surveyRespondents);


            return new Results(200, new DbDetails("Request has been sent."));
        }

        return new Results(400, "There was an issue processing the request.");
    }

    @Override
    public List<SurveyRespondents> getSurveyRespondents() {
        return respondentsRepo.findBySurveyStatus(SurveyStatus.SENT.name());
    }

    @Override
    public Results verifySurvey(String respondentId) {
        Optional<SurveyRespondents> optionalSurveyRespondents = respondentsRepo.findById(Long.valueOf(respondentId));

        if (optionalSurveyRespondents.isPresent()) {
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();

            // change status to verified
            surveyRespondents.setRespondentsStatus(SurveyRespondentStatus.VERIFIED.name()); //verification against individual respondent as opposed to entire survey

            //update on dB:
            respondentsRepo.save(surveyRespondents);

            return new Results(200, surveyRespondents);
        } else {
            return new Results(400, "Resource not found, verification not successful");
        }
    }

    @Override
    public Results rejectSurvey(String respondentId) {
        Optional<SurveyRespondents> optionalSurveyRespondents = respondentsRepo.findById(Long.valueOf(respondentId));

        if (optionalSurveyRespondents.isPresent()) {
            SurveyRespondents surveyRespondents = optionalSurveyRespondents.get();

            // change status to rejected/cancelled
            surveyRespondents.setRespondentsStatus(String.valueOf(SurveyRespondentStatus.REJECTED)); //verification against individual respondent as opposed to entire survey

            //update on dB:
            respondentsRepo.save(surveyRespondents);

            return new Results(200, surveyRespondents);
        } else {
            return new Results(400, "Resource not found, rejection not successful");
        }
    }

    private DbResponseDetails getRespondentsQuestions(String surveyId, String respondentId) {

        List<String> indicatorList = new ArrayList<>();
        Optional<Surveys> optionalSurvey = surveysRepo.findById(Long.valueOf(surveyId));
        if (optionalSurvey.isPresent()) {
            Surveys surveys = optionalSurvey.get();
            indicatorList = surveys.getIndicators();
        }

        DbPublishedVersion dbPublishedVersion = nationalTemplateService.nationalPublishedIndicators();

        String indicatorDescriptionUrl = envUrlConstants.getINDICATOR_DESCRIPTIONS();
        String indicatorDescription = WebClient.builder().baseUrl(indicatorDescriptionUrl).defaultHeaders(headers -> headers.addAll(getHeaders().getHeaders())).exchangeStrategies(ExchangeStrategies.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build()).build().get().retrieve().bodyToMono(String.class).block();

        JSONArray jsonArray = new JSONArray(indicatorDescription);

        if (dbPublishedVersion != null) {

            List<DbIndicators> newDbIndicatorsList = new ArrayList<>();
            List<DbDataEntryResponses> dataEntryResponsesList = new ArrayList<>();

            List<DbIndicators> dbIndicatorsList = dbPublishedVersion.getDetails();
            for (DbIndicators dbIndicators : dbIndicatorsList) {

                String categoryName = (String) dbIndicators.getCategoryName();
                List<DbIndicatorValues> newIndicatorList = new ArrayList<>();

                List<DbIndicatorValues> dbIndicatorValuesList = dbIndicators.getIndicators();
                for (DbIndicatorValues dbIndicatorValues : dbIndicatorValuesList) {

                    String categoryId = (String) dbIndicatorValues.getCategoryId();
                    if (indicatorList.contains(categoryId)) {
                        newIndicatorList.add(dbIndicatorValues);
                        List<DbIndicatorDataValues> dataValuesList = dbIndicatorValues.getIndicatorDataValue();

                        String description = "";
                        JSONObject jsonObject = null;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            jsonObject = jsonArray.getJSONObject(i);
                            JSONArray assessmentQuestionsArray = jsonObject.getJSONArray("assessmentQuestions");

                            if (jsonObject.has("Indicator_Code") && !jsonObject.isNull("Indicator_Code")) {
                                String Indicator_Code = jsonObject.getString("Indicator_Code");
                                if (categoryName.equals(Indicator_Code)) {
                                    if (jsonObject.has("definition") && !jsonObject.isNull("definition")) {
                                        description = jsonObject.getString("definition");
                                        dbIndicatorValues.setDescription(description);
                                    }

                                    break;
                                }
                            }
                            if (jsonObject.has("indicator_Code") && !jsonObject.isNull("indicator_Code")) {
                                String Indicator_Code = jsonObject.getString("indicator_Code");
                                if (categoryName.equals(Indicator_Code)) {
                                    if (jsonObject.has("definition") && !jsonObject.isNull("definition")) {
                                        description = jsonObject.getString("definition");
                                        dbIndicatorValues.setDescription(description);
                                    }

                                    break;
                                }
                            }

                            for (int j = 0; j < assessmentQuestionsArray.length(); j++) {
                                JSONObject question = assessmentQuestionsArray.getJSONObject(j);
                                String name = question.getString("name");

                                for (DbIndicatorDataValues dbIndicatorDataValues : dataValuesList) {
                                    String indicatorId = (String) dbIndicatorDataValues.getId();

                                    if (dbIndicatorDataValues.getName().equals(name)) {

                                        String id = question.getString("id");
                                        String code = question.getString("code");

                                        dbIndicatorDataValues.setId(id);
                                        dbIndicatorDataValues.setCode(code);
                                    }

                                    //get responses
                                    List<RespondentAnswers> respondentAnswersList = respondentAnswersRepository.findByIndicatorIdAndRespondentId(indicatorId, respondentId);
                                    if (!respondentAnswersList.isEmpty()) {

                                        String answer = "";
                                        String comments = "";
                                        String attachment = "";
                                        for (RespondentAnswers respondentAnswers : respondentAnswersList) {
                                            answer = respondentAnswers.getAnswer();
                                            comments = respondentAnswers.getComments();
                                            attachment = respondentAnswers.getAttachment();
                                        }

                                        DbDataEntryResponses dbIndicatorDataResponses = new DbDataEntryResponses(indicatorId, answer, comments, attachment);
                                        dataEntryResponsesList.add(dbIndicatorDataResponses);

                                    }

                                }

                            }
                        }
                    }
                }

                DbIndicators newDbIndicators = new DbIndicators(categoryName, newIndicatorList);
                newDbIndicatorsList.add(newDbIndicators);
            }

            return new DbResponseDetails(null, newDbIndicatorsList, dataEntryResponsesList, null);
        }
        return null;
    }


}
