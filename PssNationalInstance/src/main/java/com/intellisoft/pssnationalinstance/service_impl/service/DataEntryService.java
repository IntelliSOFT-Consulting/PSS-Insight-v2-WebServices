package com.intellisoft.pssnationalinstance.service_impl.service;


import com.intellisoft.pssnationalinstance.DbDataEntryData;
import com.intellisoft.pssnationalinstance.Results;

import java.net.URISyntaxException;

public interface DataEntryService {

    Results addDataEntry(DbDataEntryData dbDataEntryData);
    Results listDataEntry(int no, int size, String status, String dataEntryPersonId);


}
