package com.intellisoft.internationalinstance.service_impl.impl;

import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.db.Auditlogs;
import com.intellisoft.internationalinstance.db.repso.AuditlogsRepository;
import com.intellisoft.internationalinstance.service_impl.service.AuditlogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class AuditlogsServiceImpl implements AuditlogsService {
    @Autowired
    AuditlogsRepository auditlogsRepository;
    private AtomicLong versionCounter = new AtomicLong(0);

    @Override
    public Results saveAuditLog(Auditlogs auditlogs) {
        long nextVersion = versionCounter.incrementAndGet();
        auditlogs.setVersion(nextVersion);

        auditlogsRepository.save(auditlogs);
        return new Results(200, auditlogs);
    }

    @Override
    public Results fetchAuditLogs() {
        return new Results(200, auditlogsRepository.findAll());
    }

    @Override
    public Results getChangelogDetail(Long version) {

        Auditlogs auditlogs = auditlogsRepository.findByVersion(version);
        if (auditlogs != null) {
            return new Results(200, auditlogs);
        } else {
            return new Results(404, "Changelog not found");
        }
    }
}
