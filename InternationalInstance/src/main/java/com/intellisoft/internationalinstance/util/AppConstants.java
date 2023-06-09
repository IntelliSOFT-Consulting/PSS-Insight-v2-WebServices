package com.intellisoft.internationalinstance.util;

import com.intellisoft.internationalinstance.FormatterClass;
import com.itextpdf.text.BaseColor;

public class AppConstants {
    public static final String APP_NAME = "International Instance";
    public static final String INTERNATIONAL_BASE_URL=new FormatterClass().getValue().getInternationalUrl()+"/api/";
    public static  final  String METADATA_JSON_ENDPOINT=INTERNATIONAL_BASE_URL+"programs/"+new FormatterClass().getValue().getProgram()+"/metadata.json";
    public static  final  String METADATA_GROUPINGS =INTERNATIONAL_BASE_URL+"dataElementGroups.json?fields=id,name,dataElements[id,name,code]";
    public static  final String INDICATOR_DESCRIPTIONS= INTERNATIONAL_BASE_URL+"dataStore/Indicator_description/V1";
    public static final String DATA_STORE_ENDPOINT_VALUE=INTERNATIONAL_BASE_URL+"dataStore/";
    public static final String FILES_RESOURCES_ENDPOINT =INTERNATIONAL_BASE_URL+"fileResources";
    public static final String DOCS_ENDPOINT =INTERNATIONAL_BASE_URL+"documents/";
    public static final String MASTER_TEMPLATE =new FormatterClass().getValue().getMasterTemplate();
    public static final String DATA_STORE_ENDPOINT=INTERNATIONAL_BASE_URL+"dataStore/"+MASTER_TEMPLATE+"/";

    public static final String INTERNATIONAL = "";

    /*Reference sheet custom color codes*/
    public static final BaseColor PSS_BLUE = new BaseColor(0, 47, 108);
    public static final BaseColor PSS_RED = new BaseColor(186, 12, 47);

    public static  final String CREATE_DATA_ELEMENT= INTERNATIONAL_BASE_URL+"metadata";
    public static  final String ADD_DATA_ELEMENT_TO_PSS_PROGRAM= INTERNATIONAL_BASE_URL+"programStages";
    public static  final String PSS_PROGRAM_ID="T4EBleGG9mU";
    public static  final String PSS_PROGRAM_STAGE_ID="DugOfoE4Cjx";


}
