package com.intellisoft.pssnationalinstance.service_impl.service;

import com.intellisoft.pssnationalinstance.DbVersions;
import com.intellisoft.pssnationalinstance.Results;

public interface VersionEntityService {
    Results addVersion(DbVersions dbVersions);
    Results listVersions(int page, int size, boolean isLatest);
    Results updateVersion(String id, DbVersions dbVersions);
    Results deleteTemplate(String versionId);
}
