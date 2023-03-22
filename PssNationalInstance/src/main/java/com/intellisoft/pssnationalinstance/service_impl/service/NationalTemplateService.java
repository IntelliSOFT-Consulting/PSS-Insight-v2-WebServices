package com.intellisoft.pssnationalinstance.service_impl.service;

import com.intellisoft.pssnationalinstance.DbIndicators;
import com.intellisoft.pssnationalinstance.DbPublishedVersion;
import com.intellisoft.pssnationalinstance.DbVersionDate;
import com.intellisoft.pssnationalinstance.Results;

import java.util.List;

public interface NationalTemplateService {

    Results getNationalPublishedVersion();
    Results getIndicatorDescription(String pssCode);
    void savePublishedVersion(String versionId, List<DbVersionDate> indicatorList);
    DbPublishedVersion nationalPublishedIndicators();
    List<DbIndicators> getSelectedIndicators(List<DbIndicators> details,
                                             List<String> selectedIndicators);
}
