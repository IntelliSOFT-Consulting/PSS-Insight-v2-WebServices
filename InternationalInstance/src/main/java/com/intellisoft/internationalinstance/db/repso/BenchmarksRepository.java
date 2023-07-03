package com.intellisoft.internationalinstance.db.repso;

import com.intellisoft.internationalinstance.db.Benchmarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BenchmarksRepository extends JpaRepository<Benchmarks, Long> {
}
