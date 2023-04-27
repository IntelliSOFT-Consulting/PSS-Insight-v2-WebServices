package com.intellisoft.internationalinstance.util;

import com.intellisoft.internationalinstance.FormatterClass;

public class AppConstants {
    public static final String APP_NAME = "International Instance";
    public static final String INTERNATIONAL_BASE_URL=new FormatterClass().getValue().getThird()+"/api/";
    public static  final  String METADATA_JSON_ENDPOINT=INTERNATIONAL_BASE_URL+"programs/T4EBleGG9mU/metadata.json";
    public static  final  String METADATA_GROUPINGS =INTERNATIONAL_BASE_URL+"dataElementGroups.json?fields=id,name,dataElements[id,name,code]";
    public static  final String INDICATOR_DESCRIPTIONS= INTERNATIONAL_BASE_URL+"dataStore/Indicator_description/V1";
    public static final String DATA_STORE_ENDPOINT=INTERNATIONAL_BASE_URL+"dataStore/master_indicator_templates/";
    public static final String DATA_STORE_ENDPOINT_VALUE=INTERNATIONAL_BASE_URL+"dataStore/";
    public static final String FILES_RESOURCES_ENDPOINT =INTERNATIONAL_BASE_URL+"fileResources";
    public static final String DOCS_ENDPOINT =INTERNATIONAL_BASE_URL+"documents/";
    public static final String MASTER_TEMPLATE ="master_indicator_templates";

    public static final String INTERNATIONAL = "";
}
