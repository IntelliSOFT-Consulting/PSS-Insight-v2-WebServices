package com.intellisoft.internationalinstance.util;

import com.intellisoft.internationalinstance.EnvConfig;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Getter
public class EnvUrlConstants {

    public final String INDICATOR_DESCRIPTIONS;
    public final String METADATA_JSON_ENDPOINT;
    public final String METADATA_GROUPINGS;
    public final String DATA_STORE_ENDPOINT_VALUE;
    public final String FILES_RESOURCES_ENDPOINT;
    public final String DOCS_ENDPOINT;
    public final String MASTER_TEMPLATE;
    public final String DATA_STORE_ENDPOINT;
    public final String CREATE_NEW_DATA_ELEMENT;
    public final String FETCH_DATA_ELEMENTS_URL;
    public final String FETCH_OPTION_SET_URL;
    public final String FETCH_DATA_ELEMENTS_ID;
    public final String ADD_DATA_ELEMENTS_TO_PROGRAM;
    public final String CREATE_DATA_ELEMENT_GROUP;
    private final String INTERNATIONAL_BASE_URL;

    @Autowired
    public EnvUrlConstants(EnvConfig envConfig) {
        INTERNATIONAL_BASE_URL = envConfig.getValue().getInternationalUrl() + "/api/";
        METADATA_JSON_ENDPOINT = INTERNATIONAL_BASE_URL + "programs/" + envConfig.getValue().getProgram() + "/metadata.json";
        METADATA_GROUPINGS = INTERNATIONAL_BASE_URL + "dataElementGroups.json?fields=id,name,dataElements[id,name,code]";
        DATA_STORE_ENDPOINT_VALUE = INTERNATIONAL_BASE_URL + "dataStore/";
        FILES_RESOURCES_ENDPOINT = INTERNATIONAL_BASE_URL + "fileResources";
        DOCS_ENDPOINT = INTERNATIONAL_BASE_URL + "documents/";
        MASTER_TEMPLATE = envConfig.getValue().getMasterTemplate();
        DATA_STORE_ENDPOINT = INTERNATIONAL_BASE_URL + "dataStore/" + MASTER_TEMPLATE + "/";
        INDICATOR_DESCRIPTIONS = INTERNATIONAL_BASE_URL + "dataStore/Indicator_description/V1";
        CREATE_NEW_DATA_ELEMENT = INTERNATIONAL_BASE_URL + "29/metadata";
        FETCH_DATA_ELEMENTS_URL = INTERNATIONAL_BASE_URL + "dataElements";
        FETCH_OPTION_SET_URL = INTERNATIONAL_BASE_URL + "optionSets/";
        FETCH_DATA_ELEMENTS_ID = INTERNATIONAL_BASE_URL + "dataElements?filter=";
        ADD_DATA_ELEMENTS_TO_PROGRAM = INTERNATIONAL_BASE_URL + "programs/";
        CREATE_DATA_ELEMENT_GROUP = INTERNATIONAL_BASE_URL + "dataElementGroups";
    }
}
