package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.SurveyRespondents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveyRespondentsRepo extends JpaRepository<SurveyRespondents, Long> {

    List<SurveyRespondents> findBySurveyIdAndRespondentsStatus(String surveyId, String status);
    List<SurveyRespondents> findAllBySurveyId(String surveyId);
    void deleteAllBySurveyId(String surveyId);
    Optional<SurveyRespondents> findByEmailAddressAndSurveyId(String emailAddress, String surveyId);
    List<SurveyRespondents> findBySubmissionStatus(String status);
}
