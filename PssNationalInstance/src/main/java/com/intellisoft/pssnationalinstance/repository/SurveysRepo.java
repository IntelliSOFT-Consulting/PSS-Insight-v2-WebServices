package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.Surveys;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SurveysRepo extends CrudRepository<Surveys, Long> {
    List<Surveys> findByCreatorIdAndStatus(String creatorId, String status);
}
