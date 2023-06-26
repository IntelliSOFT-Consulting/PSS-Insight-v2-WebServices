package com.intellisoft.internationalinstance.db.repso;

import com.intellisoft.internationalinstance.db.Auditlogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditlogsRepository extends JpaRepository<Auditlogs, Long> {
    Auditlogs findByVersion(Long version);
}
