package com.intellisoft.pssnationalinstance.service_impl.service;

import com.intellisoft.pssnationalinstance.DbIndicatorEdit;
import com.intellisoft.pssnationalinstance.Results;

public interface IndicatorEditsService {
    Results addEdit(DbIndicatorEdit dbIndicatorEdit);
    void deleteEditByCategoryId(String categoryId);

}
