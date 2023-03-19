package com.intellisoft.nationalinstance.db.repso;

import com.intellisoft.nationalinstance.db.Surveys;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveysRepo extends JpaRepository<Surveys, Long> {

    List<Surveys> findAllByCreatorId(String creatorId);
    List<Surveys> findByCreatorIdAndStatus(String creatorId, String status);
}
