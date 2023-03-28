package com.intellisoft.internationalinstance.service_impl.service;

import com.intellisoft.internationalinstance.DbVersionData;
import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.db.VersionEntity;
import com.intellisoft.internationalinstance.model.IndicatorForFrontEnd;

import java.net.URISyntaxException;
import java.util.List;

public interface VersionService {

 Results getTemplates(int page, int size, String status);

 Results deleteTemplate(long deleteId);
 Results getVersion(long versionId);

}
