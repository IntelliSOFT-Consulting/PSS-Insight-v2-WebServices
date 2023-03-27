package com.intellisoft.internationalinstance.service_impl;

import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InternationalServiceImpl implements InternationalService{

    @Value("${dhis.international}")
    private String internationalUrl;
    @Value("${dhis.programs}")
    private String programsUrl;
    @Value("${dhis.groups}")
    private String groupsUrl;
    private final FormatterClass formatterClass = new FormatterClass();

    @Override
    public Results getIndicators() {

        String url = internationalUrl + programsUrl;
        String groupUrl = internationalUrl + groupsUrl;

        try{
            List<DbDataElements> dbDataElementsList = getDataElements(url);

            List<DbIndicatorsValue> dbIndicatorsValueList = new ArrayList<>();

            DbGroupsData dbGroupsData = GenericWebclient.getForSingleObjResponse(
                    groupUrl, DbGroupsData.class);
            if (dbGroupsData != null){
                List<DbGroupings> groupingsList = dbGroupsData.getDataElementGroups();
                for (DbGroupings dbGroupings : groupingsList){
                    List<DbDataElementsData> dataElementsList = dbGroupings.getDataElements();
                    String categoryName = dbGroupings.getName();
                    String categoryId = dbGroupings.getId();

                    List<DbDataGrouping> dbDataGroupingList = new ArrayList<>();

                    for (DbDataElementsData dataElementsData: dataElementsList){

                        String code = dataElementsData.getCode();
                        String name = dataElementsData.getName();
                        String id = dataElementsData.getId();
                        if(code != null){
                            if (!code.contains("_Comments") && !code.contains("_Uploads")){
                                String valueType = getValueType(code, dbDataElementsList);
                                if (valueType != null){
                                    DbDataGrouping dbDataGrouping = new DbDataGrouping(
                                            code,
                                            name,
                                            id,
                                            valueType);
                                    dbDataGroupingList.add(dbDataGrouping);
                                }
                            }

                        }

                    }
                    if (categoryName != null){
                        List<DbIndicatorDataValues> dbIndicatorDataValuesList = new ArrayList<>();
                        String indicatorName = formatterClass.getIndicatorName(categoryName);
                        String categoryNameData = formatterClass.mapIndicatorNameToCategory(categoryName);

                        DbIndicatorDataValues dbIndicatorDataValues = new DbIndicatorDataValues(
                                categoryId,
                                categoryName,
                                indicatorName,
                                dbDataGroupingList
                        );
                        dbIndicatorDataValuesList.add(dbIndicatorDataValues);

                        DbIndicatorsValue dbIndicatorsValue = new DbIndicatorsValue(
                                categoryNameData,
                                dbIndicatorDataValuesList);
                        dbIndicatorsValueList.add(dbIndicatorsValue);
                    }


                }
            }

            return new Results(200, dbIndicatorsValueList);

        }catch (Exception e){
            e.printStackTrace();
        }

        return new Results(400, "There was an error.");
    }

    private String getValueType(String code,List<DbDataElements> dbDataElementsList ){
        for (DbDataElements dataElements : dbDataElementsList ){
            String dataElementCode = dataElements.getCode();
            if (code.equals(dataElementCode)){
                return dataElements.getValueType();
            }
        }
        return null;
    }

    private List<DbDataElements> getDataElements(String url){

        try{

            DbMetadataJsonData dbMetadataJsonData = GenericWebclient.getForSingleObjResponse(
                    url, DbMetadataJsonData.class);
            if (dbMetadataJsonData != null){
                return dbMetadataJsonData.getDataElements();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
