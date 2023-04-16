package com.intellisoft.internationalinstance.service_impl.service;

import com.intellisoft.internationalinstance.DbIndicatorDetails;
import com.intellisoft.internationalinstance.DbIndicatorsValue;
import com.intellisoft.internationalinstance.DbVersionData;
import com.intellisoft.internationalinstance.Results;

import java.io.File;
import java.util.List;

public interface InternationalService {

    Results getIndicators();
    Results saveUpdate(DbVersionData dbVersionData);
    List<DbIndicatorsValue> getIndicatorsValues();
    String createFileResource(File file);

    Results addIndicatorDictionary(DbIndicatorDetails dbIndicatorDetails);

}
