package com.intellisoft.pssnationalinstance.service_impl.service;

import com.intellisoft.pssnationalinstance.DbPeriodConfiguration;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.db.PeriodConfiguration;

public interface PeriodConfigurationService {

    Results addPeriodConfiguration(DbPeriodConfiguration dbPeriodConfiguration);
    Results listPeriodConfiguration(int page, int size);
    Results updatePeriodConfiguration(String id, DbPeriodConfiguration dbPeriodConfiguration);
    PeriodConfiguration getConfigurationDetails(String period);
}
