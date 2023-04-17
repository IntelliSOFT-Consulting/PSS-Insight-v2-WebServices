package com.intellisoft.pssnationalinstance.util;

public class AppConstants {
    public static final String APP_NAME = "International Instance";
    public static final String INTERNATIONAL_BASE_API="http://172.104.91.116:7009/api/v1/";

    public static final String INTERNATIONAL_NOTIFICATION=INTERNATIONAL_BASE_API+"notification/";
    public static final String INTERNATIONAL_BASE_URL="http://pssinternational.intellisoftkenya.com/api/";
    public static final String NATIONAL_BASE_URL="http://pssnational.intellisoftkenya.com/api/";
    public static final String NATIONAL_BASE_PROGRAMS="http://pssnational.intellisoftkenya.com/api/programs";
    public static final String NATIONAL_BASE_ORG_UNIT= NATIONAL_BASE_URL+"organisationUnits?page=";
    public static final String NATIONAL_BASE_DOCUMENT= NATIONAL_BASE_URL+"documents";

    public static final String NATIONAL_PUBLISHED_VERSIONS = NATIONAL_BASE_URL+"dataStore/master_indicator_templates/";
    public static final String INTERNATIONAL_PUBLISHED_VERSIONS = INTERNATIONAL_BASE_URL+"dataStore/master_indicator_templates/";
    public static final String EVENTS_ENDPOINT =NATIONAL_BASE_URL+"events";
    public static final String FILES_RESOURCES_ENDPOINT =NATIONAL_BASE_URL+"fileResources";
    public static final String DOCUMENT_RESOURCES_ENDPOINT =NATIONAL_BASE_URL+"documents/";
}
