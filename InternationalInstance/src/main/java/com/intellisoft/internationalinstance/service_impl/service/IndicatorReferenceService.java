package com.intellisoft.internationalinstance.service_impl.service;

import com.intellisoft.internationalinstance.DbIndicatorDetails;
import com.intellisoft.internationalinstance.Results;

public interface IndicatorReferenceService {
    Results addIndicatorDictionary(DbIndicatorDetails dbIndicatorDetails);
    Results listIndicatorDictionary();
    Results getIndicatorValues(String uid);
}
