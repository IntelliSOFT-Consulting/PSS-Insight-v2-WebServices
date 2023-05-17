package com.intellisoft.pssnationalinstance.service_impl.service;


import com.intellisoft.pssnationalinstance.DbDataEntryData;
import com.intellisoft.pssnationalinstance.Results;

import java.net.URISyntaxException;


public interface DataEntryService {

    Results addDataEntry(DbDataEntryData dbDataEntryData);
    Results listDataEntry(int no, int size, String status);
    void saveEventData(DbDataEntryData dbDataEntryData);

    Results viewDataEntry(String id);

    Results updateDataEntry(String id, DbDataEntryData dbDataEntryData);

    Results confirmDataEntry(Long id);

    Results rejectDataEntry(Long id);
}
