package com.intellisoft.internationalinstance.service_impl.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.db.NotificationEntity;
import com.intellisoft.internationalinstance.db.VersionEntity;
import com.intellisoft.internationalinstance.db.repso.VersionRepos;
import com.intellisoft.internationalinstance.model.Response;
import com.intellisoft.internationalinstance.service_impl.service.InternationalService;
import com.intellisoft.internationalinstance.service_impl.service.NotificationService;
import com.intellisoft.internationalinstance.util.AppConstants;
import com.intellisoft.internationalinstance.util.GenericWebclient;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InternationalServiceImpl implements InternationalService {

    @Value("${dhis.international}")
    private String internationalUrl;
    @Value("${dhis.programs}")
    private String programsUrl;
    @Value("${dhis.groups}")
    private String groupsUrl;
    @Value("${dhis.indicator}")
    private String indicatorUrl;
    private final FormatterClass formatterClass = new FormatterClass();
    private final VersionRepos versionRepos;
    private final NotificationService notificationService;

    @Override
    public Results getIndicators() {

        try{
            List<DbIndicatorsValue> dbIndicatorsValueList = getIndicatorsValues();
            return new Results(200, dbIndicatorsValueList);

        }catch (Exception e){
            e.printStackTrace();
        }

        return new Results(400, "There was an error.");
    }

    public List<DbIndicatorsValue> getIndicatorsValues(){
        try{
            List<DbIndicatorsValue> dbIndicatorsValueList = new ArrayList<>();

            String url = internationalUrl + programsUrl;
            List<DbDataElements> dbDataElementsList = getDataElements(url);

            String groupUrl = internationalUrl + groupsUrl;
            DbGroupsData dbGroupsData = GenericWebclient.getForSingleObjResponse(
                    groupUrl, DbGroupsData.class);

            if (dbGroupsData != null){
                List<DbIndicatorDataValues> dbIndicatorDataValuesList = new ArrayList<>();

                List<DbGroupings> groupingsList = dbGroupsData.getDataElementGroups();
                for (DbGroupings dbGroupings : groupingsList){

                    String categoryName = dbGroupings.getName();
                    String categoryId = dbGroupings.getId();
                    List<DbDataElementsData> dataElementsList = dbGroupings.getDataElements();

                    List<DbDataGrouping> dbDataGroupingList = new ArrayList<>();
                    for (DbDataElementsData dataElementsData: dataElementsList) {

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

                    String indicatorName = formatterClass.getIndicatorName(categoryName);

                    DbIndicatorDataValues dbIndicatorDataValues = new DbIndicatorDataValues(
                            categoryId,
                            categoryName,
                            indicatorName,
                            dbDataGroupingList
                    );
                    dbIndicatorDataValuesList.add(dbIndicatorDataValues);

                }


                Map<String, List<DbIndicatorDataValues>> categoryMap = new HashMap<>();

                for (DbIndicatorDataValues dataValues: dbIndicatorDataValuesList){
                    String name = (String) dataValues.getCategoryName();
                    String categoryName = formatterClass.mapIndicatorNameToCategory(name);

                    if (!categoryName.equals("Others")){
                        if (!categoryMap.containsKey(categoryName)){
                            categoryMap.put(categoryName, new ArrayList<>());
                        }

                        categoryMap.get(categoryName).add(dataValues);
                    }

                }

                for (Map.Entry<String, List<DbIndicatorDataValues>> entry : categoryMap.entrySet()) {
                    String category = entry.getKey();
                    List<DbIndicatorDataValues> indicators = entry.getValue();
                    dbIndicatorsValueList.add(new DbIndicatorsValue(category, indicators));
                }

            }



            return dbIndicatorsValueList;
        }catch (Exception e){
            return Collections.emptyList();
        }

    }

    @Override
    public Results saveUpdate(DbVersionData dbVersionData) {

        String versionDescription = dbVersionData.getVersionDescription();
        boolean isPublished = dbVersionData.isPublished();
        List<String> indicatorList = dbVersionData.getIndicators();

        String createdBy = dbVersionData.getCreatedBy();
        String publishedBy = dbVersionData.getPublishedBy();

        String status = PublishStatus.DRAFT.name();
        if (isPublished){
            status = PublishStatus.PUBLISHED.name();
        }

        VersionEntity versionEntity = new VersionEntity();
        Long versionId = dbVersionData.getVersionId();
        if (versionId != null){
            Optional<VersionEntity> optionalVersionEntity = versionRepos.findById(versionId);
            if (optionalVersionEntity.isPresent()){
                versionEntity = optionalVersionEntity.get();

                String versionEntityStatus = versionEntity.getStatus();
                if (versionEntityStatus.equals(PublishStatus.PUBLISHED.name())){
                    return new Results(400, "You cannot edit a published version");
                }

            }
        }
        if (versionDescription != null) versionEntity.setVersionDescription(versionDescription);
        if (createdBy != null) versionEntity.setCreatedBy(createdBy);
        if (publishedBy != null) versionEntity.setPublishedBy(publishedBy);
        if (!indicatorList.isEmpty()) versionEntity.setIndicators(indicatorList);
        versionEntity.setStatus(status);


        VersionEntity savedVersionEntity = versionRepos.save(versionEntity);
        if (isPublished){

            try{
                String url = internationalUrl + programsUrl;

                List<DbIndicatorsValue> dbIndicatorsValueListNew = new ArrayList<>();

                List<DbIndicatorsValue> dbIndicatorsValueList = getIndicatorsValues();
                for (DbIndicatorsValue dbIndicatorsValue: dbIndicatorsValueList){

                    List<DbIndicatorDataValues> dbIndicatorDataValuesList = new ArrayList<>();
                    String categoryName = (String) dbIndicatorsValue.getCategoryName();

                    List<DbIndicatorDataValues> indicatorDataValuesList = dbIndicatorsValue.getIndicators();
                    for (DbIndicatorDataValues dbIndicatorDataValues: indicatorDataValuesList){
                        String categoryId = (String) dbIndicatorDataValues.getCategoryId();
                        if (indicatorList.contains(categoryId)){
                            dbIndicatorDataValuesList.add(dbIndicatorDataValues);
                        }
                    }

                    if (!dbIndicatorDataValuesList.isEmpty()){
                        DbIndicatorsValue dbIndicatorsValueNew = new DbIndicatorsValue(
                                categoryName,
                                dbIndicatorDataValuesList
                        );
                        dbIndicatorsValueListNew.add(dbIndicatorsValueNew);
                    }

                }
                DbResults dbResults = new DbResults(
                        dbIndicatorsValueListNew.size(),
                        dbIndicatorsValueListNew);

                DbMetadataJsonData dbMetadataJsonData = GenericWebclient.getForSingleObjResponse(
                        url, DbMetadataJsonData.class);
                dbMetadataJsonData.setPublishedVersion(dbResults);

                String versionNo = String.valueOf(getInternationalVersions() + 1);

                DbMetadataValue dbMetadataJson = new DbMetadataValue(
                        versionNo,
                        versionDescription,
                        dbMetadataJsonData);

                pushMetadata(dbMetadataJson, savedVersionEntity);

            }catch (Exception e){
                e.printStackTrace();
            }



        }

        return new Results(200, versionEntity);
    }

    @Async
    void pushMetadata(DbMetadataValue dbMetadataJsonData, VersionEntity savedVersionEntity){
        String groupUrl = internationalUrl + groupsUrl;
        String indicatorDescriptionUrl = internationalUrl + indicatorUrl;

        try{

            ObjectMapper objectMapper = new ObjectMapper();

            var indicatorDescription = GenericWebclient.getForSingleObjResponse(
                    indicatorDescriptionUrl, String.class);

            List<DbIndicatorDescription> indicatorDescriptionList =
                    objectMapper.readValue(indicatorDescription, List.class);

            DbGroupsData groupings = GenericWebclient.getForSingleObjResponse(
                    groupUrl, DbGroupsData.class);

            dbMetadataJsonData.getMetadata().setGroups(groupings);
            dbMetadataJsonData.getMetadata().setIndicatorDescriptions(indicatorDescriptionList);
            String versionNumber = dbMetadataJsonData.getVersion();

            var response = GenericWebclient.postForSingleObjResponse(
                    AppConstants.DATA_STORE_ENDPOINT+Integer.parseInt(versionNumber),
                    dbMetadataJsonData,
                    DbMetadataValue.class,
                    Response.class);
            if (response.getHttpStatusCode() < 200) {
//                throw new CustomException("Unable to create/update record on data store"+response);

            }else {

                savedVersionEntity.setVersionName(versionNumber);
                savedVersionEntity.setStatus(PublishStatus.PUBLISHED.name());
                versionRepos.save(savedVersionEntity);

                String message =
                        "A new template has been published by " +
                                savedVersionEntity.getPublishedBy() + " from the international instance. " +
                                "The new template has the following details: " +
                                "Version number: " + savedVersionEntity.getVersionName() + "\n" +
                                "Version description: " + savedVersionEntity.getVersionDescription() + "\n" +
                                "Number of indicators: " + savedVersionEntity.getIndicators().size();

                NotificationEntity notification = new NotificationEntity();
                notification.setTitle("New Version Published.");
                notification.setSender(savedVersionEntity.getPublishedBy());
                notification.setMessage(message);
                notificationService.createNotification(notification);

                //Create pdf
//                generatePdf(dbMetadataJsonData);

            }


        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void generatePdf(DbMetadataValue dbMetadataValue) {

        try{

            String version = dbMetadataValue.getVersion();
            String versionDescription = dbMetadataValue.getVersionDescription();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // add title to the document
            Font titleFont1 = new Font(Font.FontFamily.TIMES_ROMAN, 24, Font.BOLD);
            Font titleFont2 = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD);
            Font titleFont3 = new Font(Font.FontFamily.TIMES_ROMAN, 15, Font.BOLD);
            Paragraph title1 = new Paragraph("Pharmaceutical Products and Services", titleFont1);
            Paragraph title2 = new Paragraph("Version: "+ version, titleFont2);
            Paragraph title3 = new Paragraph("Version Description: "+ versionDescription, titleFont3);

            title1.setAlignment(Element.ALIGN_CENTER);
            title2.setAlignment(Element.ALIGN_CENTER);
            title3.setAlignment(Element.ALIGN_CENTER);

            document.add(title1);
            document.add(title2);
            document.add(title3);
            // add table to the document
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20f);
            table.setSpacingAfter(20f);

            Font tableHeaderFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
            Font tableCellFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);

            PdfPCell cell;


            DbMetadataJsonData metadataJsonData = dbMetadataValue.getMetadata();

            DbPublishedVersion publishedVersion = (DbPublishedVersion) metadataJsonData.getPublishedVersion();
            if(publishedVersion != null){
                List<DbIndicatorsData> dataList = publishedVersion.getDetails();
                for (DbIndicatorsData dbIndicatorsData : dataList){
                    String categoryName = (String) dbIndicatorsData.getCategoryName();
                    cell = new PdfPCell(new Phrase(categoryName, tableHeaderFont));
                    cell.setBorderColor(BaseColor.BLACK);
                    cell.setColspan(2);
                    cell.setPaddingLeft(10);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    table.addCell(cell);

                    List<DbIndicatorValuesData> valuesDataList = dbIndicatorsData.getIndicators();
                    for (DbIndicatorValuesData data: valuesDataList){
                        String categoryId = (String) data.getCategoryId();
                        String categoryNameCode = (String) data.getCategoryName();
                        String indicatorName = (String) data.getIndicatorName();

                        StringBuilder assessmentQuestion = new StringBuilder();
                        List<DbIndicatorDataValuesData> dataValuesDataList = data.getIndicatorDataValue();
                        for (DbIndicatorDataValuesData dbIndicatorDataValuesData: dataValuesDataList){
                            String name = (String) dbIndicatorDataValuesData.getName();
                            assessmentQuestion.append(name).append("\n");
                        }

                        cell = new PdfPCell(new Phrase("Indicator Name", tableHeaderFont));
                        cell.setBorderColor(BaseColor.BLACK);
                        cell.setPaddingLeft(10);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(cell);

                        cell = new PdfPCell(new Phrase(indicatorName, tableCellFont));
                        cell.setBorderColor(BaseColor.BLACK);
                        cell.setPaddingLeft(10);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        table.addCell(cell);

                    }
                }

            }




        }catch (Exception e){
            e.printStackTrace();
        }



    }

    private int getInternationalVersions() {

        try{
            var response = GenericWebclient.getForSingleObjResponse(
                    AppConstants.DATA_STORE_ENDPOINT,
                    List.class);

            if (!response.isEmpty()){
                return formatterClass.getNextVersion(response);
            }else {
                return 1;
            }


        }catch (Exception e){
            return 1;
        }

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
