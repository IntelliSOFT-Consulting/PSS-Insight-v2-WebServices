package com.intellisoft.nationalinstance.service_impl;

import com.intellisoft.nationalinstance.DbSurvey;
import com.intellisoft.nationalinstance.Results;
import com.intellisoft.nationalinstance.db.Surveys;

public interface SurveysService {

    Results addSurvey(DbSurvey dbSurvey);
    Results listAdminSurveys(String creatorId, String status);
    Results listRespondentsSurveys(String creatorId, String status);
    Results surveyDetails(String id);
    Surveys surveyDetailsInfo(String id);
    Results deleteSurvey(Long id);
    Results updateSurvey(Long id, DbSurvey dbSurvey);

}
