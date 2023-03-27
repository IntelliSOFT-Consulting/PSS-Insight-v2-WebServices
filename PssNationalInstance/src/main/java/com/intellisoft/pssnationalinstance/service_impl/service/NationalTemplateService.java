package com.intellisoft.pssnationalinstance.service_impl.service;

import com.intellisoft.pssnationalinstance.*;

import java.util.List;

public interface NationalTemplateService {

    Results getNationalPublishedVersion();
    Results getIndicatorDescription(String pssCode);
    void savePublishedVersion(String createdBy, String versionId, List<DbVersionDate> indicatorList);
    DbPublishedVersion nationalPublishedIndicators();
    List<DbIndicators> getSelectedIndicators(List<DbIndicators> details,
                                             List<String> selectedIndicators);
    DbMetadataJson getPublishedMetadataJson();


}
