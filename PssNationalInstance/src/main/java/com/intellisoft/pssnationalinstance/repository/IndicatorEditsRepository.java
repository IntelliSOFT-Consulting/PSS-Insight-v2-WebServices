package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.IndicatorEdits;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface IndicatorEditsRepository extends CrudRepository<IndicatorEdits, Long> {
    Optional<IndicatorEdits> findByCategoryIdAndIndicatorIdAndCreatorId(String categoryId, String indicatorId, String creatorId);
    void deleteAllByCreatorId(String creatorId);
    List<IndicatorEdits> findByCreatorId(String creatorId);

    Optional<IndicatorEdits> findFirstByCategoryIdOrderByIdDesc(String categoryId);
}
