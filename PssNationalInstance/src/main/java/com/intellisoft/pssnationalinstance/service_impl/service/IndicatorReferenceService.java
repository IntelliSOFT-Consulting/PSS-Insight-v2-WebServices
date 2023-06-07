package com.intellisoft.pssnationalinstance.service_impl.service;


import com.intellisoft.pssnationalinstance.DbIndicatorDetails;
import com.intellisoft.pssnationalinstance.Results;

public interface IndicatorReferenceService {
    Results addIndicatorDictionary(DbIndicatorDetails dbIndicatorDetails);
    Results listIndicatorDictionary();
    Results getIndicatorValues(String uid);
    Results updateDictionary(DbIndicatorDetails dbIndicatorDetails);
    Results deleteDictionary(String uid);
    Results getTopics();
}
