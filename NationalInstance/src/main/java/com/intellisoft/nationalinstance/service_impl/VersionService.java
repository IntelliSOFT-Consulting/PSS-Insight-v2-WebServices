package com.intellisoft.nationalinstance.service_impl;


import com.intellisoft.nationalinstance.DbVersionData;
import com.intellisoft.nationalinstance.Results;
import com.intellisoft.nationalinstance.db.Indicators;
import com.intellisoft.nationalinstance.db.VersionEntity;
import com.intellisoft.nationalinstance.model.IndicatorForFrontEnd;
import com.intellisoft.nationalinstance.model.Response;

import java.net.URISyntaxException;
import java.util.List;

public interface VersionService {
      Results getIndicators() throws URISyntaxException;
      VersionEntity saveDraftOrPublish(DbVersionData version) throws URISyntaxException;

      Results getTemplates(int page, int size, String status);

      Results deleteTemplate(long deleteId);
      Results getVersion(String versionId);

      Results getPublishedIndicators() throws URISyntaxException;

      Response syncVersion() throws URISyntaxException;
      List<IndicatorForFrontEnd> extractIndicators(List<Indicators> indicators);
}
