package com.intellisoft.pssnationalinstance.service_impl.service;

import com.intellisoft.pssnationalinstance.DbSurvey;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.db.Surveys;

import java.util.List;

public interface SurveysService {

    Results addSurvey(DbSurvey dbSurvey);
    Results listAdminSurveys(String creatorId, String status);
    List<String> getSurveyList(String surveyId);
    Results getSurveyDetails(String surveyId,Boolean respondents);
    Results updateSurvey(String surveyId, DbSurvey dbSurvey);

}
