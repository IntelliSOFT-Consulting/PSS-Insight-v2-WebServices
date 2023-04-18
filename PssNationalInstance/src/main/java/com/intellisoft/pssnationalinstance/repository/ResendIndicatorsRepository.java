package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.ResendIndicators;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ResendIndicatorsRepository extends JpaRepository<ResendIndicators, Long> {
    Optional<ResendIndicators> findBySurveyIdAndRespondentId(String surveyId, String respondentId);
}
