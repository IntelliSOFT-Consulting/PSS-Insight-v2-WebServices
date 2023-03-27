package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.PeriodConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PeriodConfigurationRepo extends CrudRepository<PeriodConfiguration, Long> {
    Optional<PeriodConfiguration> findByPeriod(String period);
    Page<PeriodConfiguration> findAll(Pageable pageable);
}
