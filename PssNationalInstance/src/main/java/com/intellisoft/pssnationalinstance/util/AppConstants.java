package com.intellisoft.pssnationalinstance.util;

import com.intellisoft.pssnationalinstance.FormatterClass;
import com.intellisoft.pssnationalinstance.configs.UrlConfigs;
public class AppConstants {

    public static final String APP_NAME = "International Instance";
    public static final String INTERNATIONAL_BASE_API= UrlConfigs.getInternationalBaseUrl()+"/api/v1/";
    public static final String INTERNATIONAL_BASE_URL= UrlConfigs.getInternationalBaseUrl();
    public static final String INTERNATIONAL_PUBLISHED_VERSIONS = INTERNATIONAL_BASE_URL+"dataStore/master_indicator_templates/";
    public static final String INTERNATIONAL_DOCS_ENDPOINT =INTERNATIONAL_BASE_URL+"documents/";
    public static final String INTERNATIONAL_NOTIFICATION=INTERNATIONAL_BASE_API+"notification/";
    public static final String NATIONAL_BASE_URL=new FormatterClass().getValue()+ "/api/";

    public static final String NATIONAL_BASE_PROGRAMS=NATIONAL_BASE_URL+"programs";
    public static final String NATIONAL_BASE_ORG_UNIT= NATIONAL_BASE_URL+"organisationUnits?page=";
    public static final String NATIONAL_BASE_DOCUMENT= NATIONAL_BASE_URL+"documents";
    public static final String NATIONAL_PUBLISHED_VERSIONS = NATIONAL_BASE_URL+"dataStore/master_indicator_templates/";
    public static final String EVENTS_ENDPOINT =NATIONAL_BASE_URL+"events";
    public static final String FILES_RESOURCES_ENDPOINT =NATIONAL_BASE_URL+"fileResources";
    public static final String DOCUMENT_RESOURCES_ENDPOINT =NATIONAL_BASE_URL+"documents/";

    // indicator dictionary
    public static  final String INDICATOR_DESCRIPTIONS= INTERNATIONAL_BASE_URL+"dataStore/Indicator_description/V1";
    public static final String MASTER_TEMPLATE =new FormatterClass().getMasterTemplate().getMasterTemplate();
    public static final String DATA_STORE_ENDPOINT=INTERNATIONAL_BASE_URL+"dataStore/"+MASTER_TEMPLATE+"/";

    public static final  String FETCH_BENCHMARKS_API=INTERNATIONAL_BASE_URL+"38/dataStore/Benchmarks/V1";

    public static final String PROXY_REDIRECT_URL = "https://ghoapi.azureedge.net/api/";







}




