package com.intellisoft.internationalinstance.db.repso;

import com.intellisoft.internationalinstance.db.Auditlogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditlogsRepository extends JpaRepository<Auditlogs, Long> {
    List<Auditlogs> findByVersion(Long version);
}
