package com.intellisoft.internationalinstance.service_impl.service;

import com.intellisoft.internationalinstance.DbProgramsList;
import com.intellisoft.internationalinstance.DbResults;
import com.intellisoft.internationalinstance.DbTemplateData;
import com.intellisoft.internationalinstance.Results;

public interface ProgramsService {

    /**
     * Get all available Programs
     * {{pss_dhis2_international_instance}}/programs/T4EBleGG9mU/metadata.json
     * @return
     */
    Results programList();

    /**
     * Get all namespaces in the instance
     * {{pss_dhis2_international_instance}}/33/dataStore = ["",""]
     */
    Results getNamespaces();

    /**
     * Get all keys in a provided namespace
     * {{pss_dhis2_international_instance}}33/dataStore/{{namespace}} = ["",""]
     * The keys will be used as the versions
     */
    Results getVersions(String nameSpace);

    /**
     * Get Templates values
     * {{pss_dhis2_international_instance}}33/dataStore/{{namespace}}/{{key}} = { "program":"", metadata:"{}", "description":"" }
     */
    Results getTemplates(int no);

    /**
     * Save Templates
     * {{pss_dhis2_international_instance}}33/dataStore/{{namespace_create}}/{{key_create}} = { "program":"", metadata:"{}", "description":"" }
     * Get version number as the key, description, program can act as the namespace
     */
    Results saveTemplates(DbTemplateData dbTemplateData);

}
