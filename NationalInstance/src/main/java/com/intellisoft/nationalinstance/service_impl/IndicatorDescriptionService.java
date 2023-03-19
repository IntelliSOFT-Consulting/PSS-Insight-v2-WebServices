package com.intellisoft.nationalinstance.service_impl;

import com.google.gson.JsonArray;
import com.intellisoft.nationalinstance.DbIndicatorDescription;
import com.intellisoft.nationalinstance.db.IndicatorDescription;

import java.net.URISyntaxException;
import java.util.List;

public interface IndicatorDescriptionService {
    void addIndicatorDescription(JsonArray jsonArray);
    IndicatorDescription getIndicatorDescriptionByCode(String code);
    List<DbIndicatorDescription> findAll();

}
