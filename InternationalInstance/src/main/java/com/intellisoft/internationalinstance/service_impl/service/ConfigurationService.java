package com.intellisoft.internationalinstance.service_impl.service;


import com.intellisoft.internationalinstance.DbEmailConfiguration;
import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.db.MailConfiguration;

public interface ConfigurationService {

    Results saveMailConfiguration(DbEmailConfiguration dbEmailConfiguration);
    MailConfiguration getMailConfiguration();
}
