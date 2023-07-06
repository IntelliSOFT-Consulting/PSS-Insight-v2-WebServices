package com.intellisoft.pssnationalinstance.repository;

import com.intellisoft.pssnationalinstance.db.Benchmarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BenchmarksRepository extends JpaRepository<Benchmarks, Long> {

    Optional<Benchmarks> findByIndicatorCode(Object categoryName);

    Optional<Benchmarks> findByDataElementAndOrgUnit(String dataElement, String orgUnit);
}
