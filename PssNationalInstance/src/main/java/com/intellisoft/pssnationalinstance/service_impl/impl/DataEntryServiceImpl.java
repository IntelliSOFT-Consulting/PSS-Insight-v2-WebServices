package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.db.DataEntry;
import com.intellisoft.pssnationalinstance.db.DataEntryResponses;
import com.intellisoft.pssnationalinstance.db.PeriodConfiguration;
import com.intellisoft.pssnationalinstance.repository.DataEntryRepository;
import com.intellisoft.pssnationalinstance.repository.DataEntryResponsesRepository;
import com.intellisoft.pssnationalinstance.service_impl.service.DataEntryService;
import com.intellisoft.pssnationalinstance.service_impl.service.NationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.PeriodConfigurationService;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DataEntryServiceImpl implements DataEntryService {

    private final FormatterClass formatterClass = new FormatterClass();
    private final DataEntryRepository dataEntryRepository;
    private final DataEntryResponsesRepository dataEntryResponsesRepository;
    private final NationalTemplateService nationalTemplateService;
    private final PeriodConfigurationService periodConfigurationService;

    @Override
    public Results addDataEntry(DbDataEntryData dbDataEntryData) {

        String selectedPeriod = dbDataEntryData.getSelectedPeriod();
        boolean isPublished = dbDataEntryData.isPublished();
        String dataEntryPersonId = dbDataEntryData.getDataEntryPersonId();
        String dateEntryDate = dbDataEntryData.getDataEntryDate();
        String orgUnit = dbDataEntryData.getOrgUnit();

        String status = PublishStatus.DRAFT.name();
        if (isPublished){
            status = PublishStatus.PUBLISHED.name();
        }

        DataEntry dataEntry = new DataEntry();
        dataEntry.setStatus(status);
        dataEntry.setSelectedPeriod(selectedPeriod);
        dataEntry.setDataEntryPersonId(dataEntryPersonId);
        dataEntry.setDataEntryDate(dateEntryDate);
        DataEntry dataEntryAdded = dataEntryRepository.save(dataEntry);

        List<DbDataEntryResponses> responsesList = dbDataEntryData.getResponses();
        for(DbDataEntryResponses dbDataEntryResponses: responsesList){
            String indicatorId = dbDataEntryResponses.getIndicator();
            String response = dbDataEntryResponses.getResponse();
            String comment = dbDataEntryResponses.getComment();
            String attachment = dbDataEntryResponses.getAttachment();

            DataEntryResponses dataEntryResponses = new DataEntryResponses();
            dataEntryResponses.setResponse(response);
            dataEntryResponses.setComment(comment);
            dataEntryResponses.setAttachment(attachment);
            dataEntryResponses.setIndicator(indicatorId);
            dataEntryResponses.setDataEntry(dataEntryAdded);

            dataEntryResponsesRepository.save(dataEntryResponses);
        }

        if (isPublished){

            saveEventData(dbDataEntryData);

        }


        return new Results(201, new DbDetails("Data submitted successfully."));
    }
    @Async
    public void saveEventData(DbDataEntryData dbDataEntryData){


        String selectedPeriod = dbDataEntryData.getSelectedPeriod();
        String dataEntryPersonId = dbDataEntryData.getDataEntryPersonId();
        String orgUnit = dbDataEntryData.getOrgUnit();
        List<DbDataValues> dbDataValuesList = new ArrayList<>();

        List<DbDataEntryResponses> responsesList = dbDataEntryData.getResponses();
        for(DbDataEntryResponses dbDataEntryResponses: responsesList){

            String indicatorId = dbDataEntryResponses.getIndicator();
            String response = dbDataEntryResponses.getResponse();
            String comment = dbDataEntryResponses.getComment();
            String attachment = dbDataEntryResponses.getAttachment();

            //Get the saved national template, and check for the ids and get code and add comments and uploads
            DbMetadataJson dbMetadataJson = nationalTemplateService.getPublishedMetadataJson();
            if (dbMetadataJson != null){
                DbPrograms metadata = dbMetadataJson.getMetadata();
                if (metadata != null){
                    DbGroups groups = metadata.getGroups();
                    if (groups != null){
                        List<DbDataElementGroups> dataElementGroups =
                                groups.getDataElementGroups();
                        for (DbDataElementGroups dbDataElementGroups : dataElementGroups){
                            List<DbDataElements> dataElementsList =
                                    dbDataElementGroups.getDataElements();
                            for (DbDataElements dataElements: dataElementsList){

                                String id = dataElements.getId();
                                String code = dataElements.getCode();

                                if (indicatorId.equals(id)){
                                    //Add _Comments and _Uploads
                                    String codeComments = code+"_Comments";
                                    String uploadComments = code+"_Uploads";
                                    String commentId = getCommentsUploads(codeComments, dataElementsList);
                                    String uploadId = getCommentsUploads(uploadComments, dataElementsList);

                                    if (comment != null && !commentId.equals("")){
                                        DbDataValues dbDataValues = new DbDataValues(
                                                commentId, commentId
                                        );
                                        dbDataValuesList.add(dbDataValues);
                                    }
                                    if (attachment != null && !uploadId.equals("")){
                                        DbDataValues dbDataValues = new DbDataValues(
                                                uploadId, attachment
                                        );
                                        dbDataValuesList.add(dbDataValues);
                                    }


                                }

                            }
                        }

                    }
                }
            }

            DbDataValues dbDataValues = new DbDataValues(indicatorId, response);
            dbDataValuesList.add(dbDataValues);

        }

        if (selectedPeriod != null){
            PeriodConfiguration periodConfiguration =
                    periodConfigurationService.getConfigurationDetails(selectedPeriod);
            String status = PublishStatus.DRAFT.name();

            if (periodConfiguration != null){
                boolean isCompleted = periodConfiguration.isCompleted();
                if (isCompleted){
                    status = PublishStatus.COMPLETED.name();
                }
            }

            DbDataEntry dataEntry = new DbDataEntry(
                    "T4EBleGG9mU",
                    orgUnit,
                    selectedPeriod + "-01-" + "01",
                    status,
                    dataEntryPersonId,
                    dbDataValuesList);



            try {

                var response = GenericWebclient.postForSingleObjResponse(
                        AppConstants.EVENTS_ENDPOINT,
                        dataEntry,
                        DbDataEntry.class,
                        String.class);

                System.out.println("************");
                System.out.println(response);
                System.out.println("************");


            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }
    private String getCommentsUploads(String codeComments, List<DbDataElements> dataElementsList) {

        String id = "";
        for (DbDataElements dataElements : dataElementsList){
            String code = dataElements.getCode();
            if (codeComments.equals(code)){
                id = dataElements.getId();
                break;
            }
        }
        return id;
    }

    @Override
    public Results listDataEntry(int no, int size, String status, String dataEntryPersonId) {

        List<DataEntry> dataEntryList =
                getPagedDataEntryData(
                        no,
                        size,
                        "",
                        "",
                        status,
                        dataEntryPersonId);

        List<DbDataEntryResponse> responseList = new ArrayList<>();
        for (DataEntry dataEntry : dataEntryList){

            List<DataEntryResponses> dataEntryResponseList =
                    dataEntryResponsesRepository.findByDataEntry(dataEntry);

            DbDataEntryResponse dbDataEntryResponse = new DbDataEntryResponse(
                    dataEntry.getId(),
                    dataEntry.getSelectedPeriod(),
                    dataEntry.getStatus(),
                    dataEntry.getDataEntryPersonId(),
                    dataEntry.getDataEntryDate(),
                    dataEntry.getCreatedAt(),
                    dataEntryResponseList);
            responseList.add(dbDataEntryResponse);
        }

        DbResults dbResults = new DbResults(
                responseList.size(),
                responseList);

        return new Results(200, dbResults);
    }

    private List<DataEntry> getPagedDataEntryData(
            int pageNo,
            int pageSize,
            String sortField,
            String sortDirection,
            String status,
            String userId) {
        String sortPageField = "";
        String sortPageDirection = "";

        if (sortField.equals("")){sortPageField = "createdAt"; }else {sortPageField = sortField;}
        if (sortDirection.equals("")){sortPageDirection = "DESC"; }else {sortPageDirection = sortField;}

        Sort sort = sortPageDirection.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortPageField).ascending() : Sort.by(sortPageField).descending();
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<DataEntry> page =
                dataEntryRepository.findAllByStatusAndDataEntryPersonId(
                        status, userId, pageable);

        return page.getContent();
    }
}
