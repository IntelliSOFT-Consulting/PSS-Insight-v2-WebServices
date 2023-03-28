package com.intellisoft.pssnationalinstance.service_impl.service;

import com.intellisoft.pssnationalinstance.DbMetadataJson;
import com.intellisoft.pssnationalinstance.DbPublishedVersion;
import com.intellisoft.pssnationalinstance.Results;

import java.net.URISyntaxException;

public interface InternationalTemplateService {

    /**
     * Get International published version
     */
    Results getInternationalIndicators();
    int getVersions(String url) throws URISyntaxException;
    DbMetadataJson getPublishedData(String url);
    DbPublishedVersion interNationalPublishedIndicators();
    DbMetadataJson getIndicators(String url);

}
