package com.intellisoft.nationalinstance.service_impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.intellisoft.nationalinstance.DbIndicatorDescription;
import com.intellisoft.nationalinstance.db.IndicatorDescription;
import com.intellisoft.nationalinstance.db.repso.IndicatorDescriptionRepo;
import com.intellisoft.nationalinstance.util.AppConstants;
import com.intellisoft.nationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.aspectj.apache.bcel.classfile.Module;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndicatorDescriptionServiceImpl implements IndicatorDescriptionService{

    private final IndicatorDescriptionRepo indicatorDescriptionRepo;


    @Async
    @Override
    public void addIndicatorDescription(JsonArray jsonArray) {

        for (int i = 0; i < jsonArray.size(); i++){

            JsonElement element = jsonArray.get(i);
            String code = element.getAsJsonObject().get("Indicator_Code").getAsString();
            String description = element.getAsJsonObject().get("Description").getAsString();
            Optional<IndicatorDescription> optionalIndicatorDescription =
                    indicatorDescriptionRepo.findByCode(code);

            if (optionalIndicatorDescription.isPresent()){
                IndicatorDescription indicatorDescriptionUpdate = optionalIndicatorDescription.get();
                indicatorDescriptionUpdate.setDescription(description);
                indicatorDescriptionRepo.save(indicatorDescriptionUpdate);
            }else {
                IndicatorDescription indicatorDescription = new IndicatorDescription();
                indicatorDescription.setCode(code);
                indicatorDescription.setDescription(description);
                indicatorDescriptionRepo.save(indicatorDescription);
            }

        }


    }

    @Override
    public IndicatorDescription getIndicatorDescriptionByCode(String code) {

        Optional<IndicatorDescription> optionalIndicatorDescription =
                indicatorDescriptionRepo.findByCode(code);
        return optionalIndicatorDescription.orElse(null);
    }

    @Override
    public List<DbIndicatorDescription> findAll() {

        List<DbIndicatorDescription> dbIndicatorDescriptionList = new ArrayList<>();
        List<IndicatorDescription> indicatorList = indicatorDescriptionRepo.findAll();
        for (int i = 0; i < indicatorList.size(); i++){

            String description = indicatorList.get(i).getDescription();
            String indicator = indicatorList.get(i).getCode();

            DbIndicatorDescription dbIndicatorDescription = new DbIndicatorDescription(description, indicator);
            dbIndicatorDescriptionList.add(dbIndicatorDescription);
        }

        return dbIndicatorDescriptionList;
    }

}
