package com.intellisoft.internationalinstance.db.repso;

import com.intellisoft.internationalinstance.db.Benchmarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BenchmarksRepository extends JpaRepository<Benchmarks, Long> {
    Optional<Benchmarks> findByDataElementAndOrgUnit(String dataElement, String orgUnit);
}
