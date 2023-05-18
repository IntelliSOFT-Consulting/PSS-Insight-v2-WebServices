package com.intellisoft.pssnationalinstance.util;


import com.intellisoft.pssnationalinstance.FormatterClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
public class AppConstants {


    public static final String APP_NAME = "International Instance";
//    public static final String INTERNATIONAL_BASE_API="http://172.104.91.116:7009/api/v1/";

    public static final String INTERNATIONAL_BASE_API="localhost:7009/api/v1/";
    public static final String INTERNATIONAL_BASE_URL="http://pssinternational.intellisoftkenya.com/api/";
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






}




