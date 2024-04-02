package com.intellisoft.pssnationalinstance.util;

import com.intellisoft.pssnationalinstance.EnvConfig;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Getter
public class EnvUrlConstants {

    private final String INTERNATIONAL_BASE_URL;
    private final String INTERNATIONAL_BASE_API;
    private final String INTERNATIONAL_PUBLISHED_VERSIONS;
    private final String INTERNATIONAL_DOCS_ENDPOINT;
    private final String INTERNATIONAL_NOTIFICATION;
    private final String NATIONAL_BASE_URL;
    private final String NATIONAL_BASE_PROGRAMS;
    private final String NATIONAL_BASE_ORG_UNIT;
    private final String NATIONAL_BASE_DOCUMENT;
    private final String NATIONAL_PUBLISHED_VERSIONS;
    private final String EVENTS_ENDPOINT;
    private final String FILES_RESOURCES_ENDPOINT;
    private final String DOCUMENT_RESOURCES_ENDPOINT;
    private final String INDICATOR_DESCRIPTIONS;
    private final String MASTER_TEMPLATE;
    private final String DATA_STORE_ENDPOINT;
    private final String FETCH_BENCHMARKS_API;

    @Autowired
    public EnvUrlConstants(EnvConfig envConfig) {
        INTERNATIONAL_BASE_URL = envConfig.getValue().getInternationalUrl() + "/api/"; //DHIS-APIs-Path
        INTERNATIONAL_BASE_API = INTERNATIONAL_BASE_URL + "v1/"; //global-instance APi Path
        INTERNATIONAL_PUBLISHED_VERSIONS = INTERNATIONAL_BASE_URL + "dataStore/master_indicator_templates/";
        INTERNATIONAL_DOCS_ENDPOINT = INTERNATIONAL_BASE_URL + "documents/";
        INTERNATIONAL_NOTIFICATION = INTERNATIONAL_BASE_API + "notification/";
        NATIONAL_BASE_URL = envConfig.getValue().getServerUrl() + "/api/";
        NATIONAL_BASE_PROGRAMS = NATIONAL_BASE_URL + "programs";
        NATIONAL_BASE_ORG_UNIT = NATIONAL_BASE_URL + "organisationUnits?page=";
        NATIONAL_BASE_DOCUMENT = NATIONAL_BASE_URL + "documents";
        NATIONAL_PUBLISHED_VERSIONS = NATIONAL_BASE_URL + "dataStore/master_indicator_templates/";
        EVENTS_ENDPOINT = NATIONAL_BASE_URL + "events";
        FILES_RESOURCES_ENDPOINT = NATIONAL_BASE_URL + "fileResources";
        DOCUMENT_RESOURCES_ENDPOINT = NATIONAL_BASE_URL + "documents/";
        INDICATOR_DESCRIPTIONS = INTERNATIONAL_BASE_URL + "dataStore/Indicator_description/V1";
        MASTER_TEMPLATE = envConfig.getValue().getMasterTemplate();
        DATA_STORE_ENDPOINT = INTERNATIONAL_BASE_URL + "dataStore/" + MASTER_TEMPLATE + "/";
        FETCH_BENCHMARKS_API = INTERNATIONAL_BASE_URL + "38/dataStore/Benchmarks/V1";
    }
}
