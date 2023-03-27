package com.intellisoft.pssnationalinstance.service_impl.service;

import com.intellisoft.pssnationalinstance.DbIndicatorEdit;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.db.IndicatorEdits;

import java.util.List;

public interface IndicatorEditsService {
    Results addEdit(DbIndicatorEdit dbIndicatorEdit);
    void deleteEditByCategoryId(String creatorId);

    List<IndicatorEdits> getIndicatorEditsCategoryCreator(String categoryId, String creatorId);
    List<IndicatorEdits> getIndicatorEditsCreator(String creatorId);


}
