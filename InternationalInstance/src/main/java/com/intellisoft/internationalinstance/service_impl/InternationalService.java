package com.intellisoft.internationalinstance.service_impl;

import com.intellisoft.internationalinstance.DbIndicatorsValue;
import com.intellisoft.internationalinstance.DbVersionData;
import com.intellisoft.internationalinstance.Results;

import java.util.List;

public interface InternationalService {

    Results getIndicators();
    Results saveUpdate(DbVersionData dbVersionData);
    List<DbIndicatorsValue> getIndicatorsValues();

}
