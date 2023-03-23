package com.intellisoft.pssnationalinstance.service_impl.service;

import com.intellisoft.pssnationalinstance.DbSurveyRespondent;
import com.intellisoft.pssnationalinstance.DbVerifySurvey;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.db.SurveyRespondents;
import com.intellisoft.pssnationalinstance.db.Surveys;

import java.util.List;

public interface SurveyRespondentsService {

    /**
     * Creates a new entry to the survey respondents table
     * Takes in emailAddress, expiryDateTime, surveyId
     * Create a password for the different users
     * @param dbSurveyRespondent
     * @return
     */
    Results addSurveyRespondent(DbSurveyRespondent dbSurveyRespondent);

    /**
     * This displays all the respondents in a survey id
     * @param surveyId
     * @return
     */
    Results listSurveyRespondent(String surveyId, String status);

    List<SurveyRespondents> getSurveyRespondents(String surveyId, String status);

    /**
     * Delete all Records associated with a survey id
     * @param surveyId
     * @return
     */
    Results deleteSurveyRespondent(String surveyId);

    /**
     * Delete particular respondent details
     * @return
     */
    Results deleteRespondent(String id);

    /**
     * Check if the password match what we have and if the time has expired
     * @param dbVerifySurvey
     * @return
     */
    Results verifyPassword(DbVerifySurvey dbVerifySurvey);

//
//    /**
//     * This gets all the assigned questions for the person
//     */
//    Results getAssignedSurvey(String respondentId);
//
//
//    /**
//     * This gets all the answers for the respondent
//     */
//    Results getAssignedAnswers(String respondentId);
//
//
//    /**
//     * Save Respondents survey responses
//     */
//    Results saveResponse(DbResponse dbResponse);
//
//    /**
//     * Request new link with comment
//     */
//    Results requestLink(DbRequestLink dbRequestLink);

}