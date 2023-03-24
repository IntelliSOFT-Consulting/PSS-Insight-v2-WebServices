package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.RespondentAnswers;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RespondentAnswersRepository extends CrudRepository<RespondentAnswers, Long> {
    Optional<RespondentAnswers> findByIndicatorIdAndRespondentId(String indicatorId, String respondentId);
    List<RespondentAnswers> findByRespondentIdAndStatus(String respondentId, String status);
}
