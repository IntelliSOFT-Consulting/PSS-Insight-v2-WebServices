package com.intellisoft.nationalinstance.db.repso;

import com.intellisoft.nationalinstance.db.RespondentAnswers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RespondentAnswersRepo extends JpaRepository<RespondentAnswers, Long> {
    Optional<RespondentAnswers> findByIndicatorIdAndRespondentId(String indicatorId, String respondentId);
}
