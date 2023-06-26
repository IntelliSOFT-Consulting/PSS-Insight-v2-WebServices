package com.intellisoft.internationalinstance.service_impl.service;

import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.db.Auditlogs;

public interface AuditlogsService {
    Results saveAuditLog(Auditlogs auditlogs);

    Results fetchAuditLogs();

    Results getChangelogDetail(Long version);
}
