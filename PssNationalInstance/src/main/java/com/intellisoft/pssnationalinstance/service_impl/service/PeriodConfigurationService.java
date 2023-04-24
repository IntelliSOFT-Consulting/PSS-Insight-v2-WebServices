package com.intellisoft.pssnationalinstance.service_impl.service;

import com.intellisoft.pssnationalinstance.DbEmailConfiguration;
import com.intellisoft.pssnationalinstance.DbPeriodConfiguration;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.db.MailConfiguration;
import com.intellisoft.pssnationalinstance.db.PeriodConfiguration;
import com.sendgrid.helpers.mail.Mail;

public interface PeriodConfigurationService {

    Results addPeriodConfiguration(DbPeriodConfiguration dbPeriodConfiguration);
    Results listPeriodConfiguration(int page, int size);
    Results updatePeriodConfiguration(String id, DbPeriodConfiguration dbPeriodConfiguration);
    PeriodConfiguration getConfigurationDetails(String period);
    Results saveMailConfiguration(DbEmailConfiguration dbEmailConfiguration);
    MailConfiguration getMailConfiguration();
}
