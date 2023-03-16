package com.intellisoft.nationalinstance.db.repso;

import com.intellisoft.nationalinstance.db.Indicators;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndicatorsRepo extends CrudRepository<Indicators, Long> {
    List<Indicators> findAll();
    @Query("SELECT i.metadata FROM Indicators i WHERE i.indicatorId in :indicatorIds")
    List<String> findMetadataByIndicatorIds(@Param("indicatorIds") List<String> indicatorIds);

    @Query("SELECT i FROM Indicators i WHERE i.indicatorId in :indicatorIds")
    List<Indicators> findIndicatorByIndicatorIds(@Param("indicatorIds") List<String> indicatorIds);
    @Query("SELECT i.metadata FROM Indicators i WHERE i.indicatorId in :indicatorIds")
    List<String> findByIndicatorIds(@Param("indicatorIds") List<String> indicatorIds);

    Optional<Indicators> findByIndicatorId(String indicatorId);

    Boolean existsByIndicatorId(String indicatorId);


}
