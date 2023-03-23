package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.db.SurveyRespondents;
import com.intellisoft.pssnationalinstance.db.Surveys;
import com.intellisoft.pssnationalinstance.repository.SurveyRespondentsRepo;
import com.intellisoft.pssnationalinstance.repository.SurveysRepo;
import com.intellisoft.pssnationalinstance.service_impl.service.NationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.SurveyRespondentsService;
import com.intellisoft.pssnationalinstance.service_impl.service.SurveysService;
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

        surveyRespondents.setStatus(SurveySubmissionStatus.DRAFT.name());
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
                    String.class);
            System.out.println("RESPONSE FROM REMOTE: {}"+response);



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
        List<SurveyRespondents> surveyRespondentsList =
                respondentsRepo.findBySurveyIdAndStatus(surveyId, status);
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
                //Return questions
                String surveyId = surveyRespondents.getSurveyId();

                List<String> indicatorList = new ArrayList<>();
                Optional<Surveys> optionalSurvey = surveysRepo.findById(Long.valueOf(surveyId));
                if (optionalSurvey.isPresent()){
                    Surveys surveys = optionalSurvey.get();
                    indicatorList = surveys.getIndicators();
                }

                DbPublishedVersion dbPublishedVersion =
                        nationalTemplateService.nationalPublishedIndicators();
                if (dbPublishedVersion != null){

                    List<DbIndicators> newDbIndicatorsList = new ArrayList<>();

                    List<DbIndicators> dbIndicatorsList = dbPublishedVersion.getDetails();
                    for (DbIndicators dbIndicators : dbIndicatorsList){

                        String categoryName = (String) dbIndicators.getCategoryName();
                        List<DbIndicatorValues> newIndicatorList = new ArrayList<>();

                        List<DbIndicatorValues> dbIndicatorValuesList = dbIndicators.getIndicators();
                        for (DbIndicatorValues dbIndicatorValues : dbIndicatorValuesList){

                            String categoryId = (String) dbIndicatorValues.getCategoryId();
                            if (indicatorList.contains(categoryId)){
                                newIndicatorList.add(dbIndicatorValues);
                            }
                        }

                        DbIndicators newDbIndicators = new DbIndicators(
                                categoryName, newIndicatorList);
                        newDbIndicatorsList.add(newDbIndicators);

                    }

                    DbPublishedVersion dbPublishedVersionUpdate = new DbPublishedVersion(
                            newDbIndicatorsList.size(),
                            newDbIndicatorsList);
                    return new Results(200, dbPublishedVersionUpdate);


                }


            }
        }

        return new Results(400, "Password authentication failed.");
    }


}
