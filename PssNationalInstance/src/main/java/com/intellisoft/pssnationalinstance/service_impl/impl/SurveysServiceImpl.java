package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.db.RespondentAnswers;
import com.intellisoft.pssnationalinstance.db.SurveyRespondents;
import com.intellisoft.pssnationalinstance.db.Surveys;
import com.intellisoft.pssnationalinstance.repository.RespondentAnswersRepository;
import com.intellisoft.pssnationalinstance.repository.SurveysRepo;
import com.intellisoft.pssnationalinstance.service_impl.service.NationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.SurveyRespondentsService;
import com.intellisoft.pssnationalinstance.service_impl.service.SurveysService;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SurveysServiceImpl implements SurveysService {

    private final SurveysRepo surveysRepo;
    private final SurveyRespondentsService surveyRespondentsService;
    private final FormatterClass formatterClass = new FormatterClass();
    private final NationalTemplateService nationalTemplateService;
    private final RespondentAnswersRepository respondentAnswersRepository;

    @Override
    public Results addSurvey(DbSurvey dbSurvey) {
        String surveyName = dbSurvey.getSurveyName();
        String surveyDescription = dbSurvey.getSurveyDescription();
        boolean isSaved = dbSurvey.isSaved();
        String creatorId = dbSurvey.getCreatorId();
        String landingPage = dbSurvey.getSurveyLandingPage();
        List<String> indicatorList = dbSurvey.getIndicators();

        String status = SurveyStatus.SAVED.name();
//        if (isSaved)
//            status = SurveyStatus.SENT.name();

        String versionNumber = String.valueOf(nationalTemplateService.getCurrentVersion(AppConstants.NATIONAL_PUBLISHED_VERSIONS));


        Surveys surveys = new Surveys();
        surveys.setName(surveyName);
        surveys.setDescription(surveyDescription);
        surveys.setVersionNumber(versionNumber);
        surveys.setStatus(status);
        surveys.setLandingPage(landingPage);
        surveys.setCreatorId(creatorId);
        surveys.setIndicators(indicatorList);
        surveysRepo.save(surveys);

        return new Results(201, surveys);    }

    @Override
    public Results listAdminSurveys(String creatorId, String status) {

        List<DbSurveyDetails> dbSurveyDetailsList = new ArrayList<>();
        String surveyStatus = SurveyStatus.SENT.name();
        List<Surveys> surveysList = surveysRepo.findByCreatorIdAndStatus(creatorId, surveyStatus);
        for (Surveys surveys : surveysList){

            Long id = surveys.getId();
            String surveyName = surveys.getName();
            String surveyDesc = surveys.getDescription();
            String surveyStatusValue = surveys.getStatus();
            String landingPage = surveys.getLandingPage();
            //Get respondents under this with required status

            List<DbRespondent> dbRespondentList = new ArrayList<>();
            List<SurveyRespondents> respondentsList =
                    surveyRespondentsService.getSurveyRespondents(String.valueOf(id), status);

            for (SurveyRespondents surveyRespondents : respondentsList){

                String respId = String.valueOf(surveyRespondents.getId());
                String emailAddress = surveyRespondents.getEmailAddress();
                String date = String.valueOf(surveyRespondents.getCreatedAt());
                String expiryDate = surveyRespondents.getExpiryTime();


                if (status.equals(SurveySubmissionStatus.EXPIRED.name())){

                    String respondentStatus = surveyRespondents.getRespondentsStatus();

                    boolean isExpired = formatterClass.isPastToday(expiryDate);
                    if (isExpired){
                        DbRespondent dbRespondent = new DbRespondent(respId, emailAddress, date,
                                null, null);
                        //Link has expired
                        dbRespondent.setExpiryDate(expiryDate);
                        dbRespondent.setNewLinkRequested(
                                respondentStatus.equals(SurveyRespondentStatus.RESEND_REQUEST.name()));
                        dbRespondentList.add(dbRespondent);

                    }

                }else {
                    DbRespondent dbRespondent = new DbRespondent(respId, emailAddress, date,
                            expiryDate, null);
                    dbRespondentList.add(dbRespondent);

                }

            }

            if (!dbRespondentList.isEmpty()){
                DbSurveyDetails details = new DbSurveyDetails(
                        String.valueOf(id),
                        surveyName,
                        surveyStatusValue,
                        surveyDesc,
                        landingPage,
                        dbRespondentList);
                dbSurveyDetailsList.add(details);
            }


        }

        DbResults dbResults = new DbResults(
                dbSurveyDetailsList.size(),
                dbSurveyDetailsList);

        return new Results(200, dbResults);

    }

    @Override
    public List<String> getSurveyList(String surveyId) {
        Optional<Surveys> optionalSurvey = surveysRepo.findById(Long.valueOf(surveyId));
        if (optionalSurvey.isPresent()){
            Surveys surveys = optionalSurvey.get();
            return surveys.getIndicators();
        }

        return Collections.emptyList();
    }

    @Override
    public Results getSurveyDetails(String surveyId, Boolean isRespondents) {

        Optional<Surveys> optionalSurvey = surveysRepo.findById(Long.valueOf(surveyId));
        if (optionalSurvey.isPresent()){
            Surveys surveys = optionalSurvey.get();
            String versionNumber = surveys.getVersionNumber();

            DbPublishedVersion dbPublishedVersion =
                    getPublishedData(AppConstants.NATIONAL_PUBLISHED_VERSIONS+versionNumber);
            List<String> indicators = surveys.getIndicators();

            if (dbPublishedVersion != null){
                List<DbIndicators> dbIndicatorsList = dbPublishedVersion.getDetails();
                List<DbIndicators> selectedIndicators =
                        nationalTemplateService.getSelectedIndicators(dbIndicatorsList, indicators);

                List<DbSurveyRespondentDataDerails> dbSurveyRespondentDataDerailsList = new ArrayList<>();
                if (isRespondents){
                    List<SurveyRespondents> surveyRespondentsList =
                            surveyRespondentsService.getSurveyRespondents(surveyId, "ALL");
                    for (SurveyRespondents surveyRespondents: surveyRespondentsList){
                        DbSurveyRespondentDataDerails dbSurveyRespondentDataDerails =
                                new DbSurveyRespondentDataDerails(
                                    surveyRespondents.getId(),
                                    surveyRespondents.getEmailAddress(),
                                    surveyRespondents.getExpiryTime(),
                                    surveyId,
                                    surveyRespondents.getCustomUrl(),
                                    surveyRespondents.getRespondentsStatus()
                                );
                        dbSurveyRespondentDataDerailsList.add(dbSurveyRespondentDataDerails);
                    }
                }

                DbSurveyData data = new DbSurveyData(
                        surveys.getId(),
                        surveys.getName(),
                        surveys.getDescription(),
                        surveys.getLandingPage(),
                        surveys.getStatus(),
                        surveys.getCreatorId(),
                        surveys.getCreatedAt(),
                        selectedIndicators,
                        dbSurveyRespondentDataDerailsList);
                return new Results(200, data);
            }

        }

        return new Results(400, "There was an issue with the request.");
    }

    @Override
    public Results updateSurvey(String surveyId, DbSurvey dbSurvey) {

        Optional<Surveys> optionalSurveys = surveysRepo.findById(Long.valueOf(surveyId));
        if (optionalSurveys.isPresent()){
            Surveys surveys = optionalSurveys.get();

            String surveyName = dbSurvey.getSurveyName();
            String surveyDescription = dbSurvey.getSurveyDescription();
            boolean isSaved = dbSurvey.isSaved();
            String landingPage = dbSurvey.getSurveyLandingPage();
            List<String> indicatorList = dbSurvey.getIndicators();

            if (surveyName != null) surveys.setName(surveyName);
            if (surveyDescription != null) surveys.setDescription(surveyDescription);
            if (landingPage != null) surveys.setLandingPage(landingPage);
            if (surveyName != null) surveys.setStatus(surveyName);
            if (!indicatorList.isEmpty()) surveys.setIndicators(indicatorList);

            Surveys surveysDetails = surveysRepo.save(surveys);
            return new Results(200 ,surveysDetails);

        }
        return new Results(400, "Resource not found");


    }

    public DbPublishedVersion getPublishedData(String url) {

        try {
            DbMetadataJson dbMetadataJson = GenericWebclient.getForSingleObjResponse(
                    url, DbMetadataJson.class);
            if (dbMetadataJson != null){
                DbPrograms dbPrograms = dbMetadataJson.getMetadata();
                if (dbPrograms != null){
                    DbPublishedVersion dbPublishedVersion = dbPrograms.getPublishedVersion();
                    if (dbPublishedVersion != null){
                        return dbPublishedVersion;
                    }
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }
}
