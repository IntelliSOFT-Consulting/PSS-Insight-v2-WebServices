package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.db.SurveyRespondents;
import com.intellisoft.pssnationalinstance.db.Surveys;
import com.intellisoft.pssnationalinstance.repository.SurveysRepo;
import com.intellisoft.pssnationalinstance.service_impl.service.SurveyRespondentsService;
import com.intellisoft.pssnationalinstance.service_impl.service.SurveysService;
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

    @Override
    public Results addSurvey(DbSurvey dbSurvey) {
        String surveyName = dbSurvey.getSurveyName();
        String surveyDescription = dbSurvey.getSurveyDescription();
        boolean isSaved = dbSurvey.isSaved();
        String creatorId = dbSurvey.getCreatorId();
        List<String> indicatorList = dbSurvey.getIndicators();

        String status = SurveyStatus.SAVED.name();
        if (isSaved)
            status = SurveyStatus.SENT.name();


        Surveys surveys = new Surveys();
        surveys.setName(surveyName);
        surveys.setDescription(surveyDescription);
        surveys.setStatus(status);
        surveys.setCreatorId(creatorId);
        surveys.setIndicators(indicatorList);
        surveysRepo.save(surveys);

        return new Results(201, surveys);    }

    @Override
    public Results listAdminSurveys(String creatorId, String status) {

        /**
         * TODO: Check if status is expired and add more parameters
         */

        List<DbSurveyDetails> dbSurveyDetailsList = new ArrayList<>();
        String surveyStatus = SurveyStatus.SENT.name();
        List<Surveys> surveysList = surveysRepo.findByCreatorIdAndStatus(creatorId, surveyStatus);
        for (Surveys surveys : surveysList){

            Long id = surveys.getId();
            String surveyName = surveys.getName();
            //Get respondents under this with required status

            List<DbRespondent> dbRespondentList = new ArrayList<>();
            List<SurveyRespondents> respondentsList =
                    surveyRespondentsService.getSurveyRespondents(String.valueOf(id), status);
            for (SurveyRespondents surveyRespondents : respondentsList){

                String respId = String.valueOf(surveyRespondents.getId());
                String emailAddress = surveyRespondents.getEmailAddress();
                String date = String.valueOf(surveyRespondents.getCreatedAt());
                DbRespondent dbRespondent = new DbRespondent(respId, emailAddress, date);
                dbRespondentList.add(dbRespondent);
            }

            DbSurveyDetails details = new DbSurveyDetails(
                    String.valueOf(id),
                    surveyName,
                    status,
                    dbRespondentList);
            dbSurveyDetailsList.add(details);
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
}
