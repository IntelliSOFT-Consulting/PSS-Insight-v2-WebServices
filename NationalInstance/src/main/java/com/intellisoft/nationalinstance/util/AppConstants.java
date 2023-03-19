package com.intellisoft.nationalinstance.util;

public class AppConstants {
    public static final String APP_NAME = "International Instance";
    public static final String INTERNATIONAL_BASE_URL="http://pssinternational.intellisoftkenya.com/api/";
    public static final String PUBLISHED_TEMPLATE="http://pssinternational.intellisoftkenya.com/api/";

    public static final String NATIONAL_BASE_URL="http://pssnational.intellisoftkenya.com/api/";
    public static  final  String METADATA_GROUPINGS =INTERNATIONAL_BASE_URL+"dataElementGroups.json?fields=id,name,dataElements[id,name,code]";
    public static  final  String METADATA_JSON_ENDPOINT=NATIONAL_BASE_URL+"programs/T4EBleGG9mU/metadata.json";
    public static  final  String INDICATOR_DESCRIPTION_ENDPOINT=INTERNATIONAL_BASE_URL+"dataStore/Indicator_description/V1";

    public static  final  String INTERNATIONAL_METADATA_ENDPOINT =INTERNATIONAL_BASE_URL+"programs/T4EBleGG9mU/metadata.json";
    //Change to national url
    public static final String DATA_STORE_ENDPOINT = NATIONAL_BASE_URL+"dataStore/master_indicator_templates/";
    public static final String DATA_STORE_ENDPOINT_INT = INTERNATIONAL_BASE_URL+"dataStore/master_indicator_templates/";

    public static  final  String NATIONAL_INSTANCE_ENDPOINT ="NATIONAL_BASE_";
    public static final String EVENTS_ENDPOINT =NATIONAL_BASE_URL+"events";
}
