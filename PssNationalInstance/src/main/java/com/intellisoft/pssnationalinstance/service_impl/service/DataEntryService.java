package com.intellisoft.pssnationalinstance.service_impl.service;


import com.intellisoft.pssnationalinstance.DbDataEntryData;
import com.intellisoft.pssnationalinstance.DbResendDataEntry;
import com.intellisoft.pssnationalinstance.Results;
import java.net.URISyntaxException;


public interface DataEntryService {

    Results addDataEntry(DbDataEntryData dbDataEntryData);
    Results listDataEntry(int no, int size, String status, String dataEntryPersonId);
    void saveEventData(DbDataEntryData dbDataEntryData);

    Results viewDataEntry(String id) throws URISyntaxException;

    Results updateDataEntry(String id, DbDataEntryData dbDataEntryData);

    Results confirmDataEntry(Long id);

    Results rejectDataEntry(Long id);

    Results resendRoutineDataEntry(Long id, DbResendDataEntry resendDataEntry);
}
