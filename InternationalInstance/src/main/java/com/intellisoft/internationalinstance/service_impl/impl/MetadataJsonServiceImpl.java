package com.intellisoft.internationalinstance.service_impl.impl;

import com.intellisoft.internationalinstance.db.MetadataJson;
import com.intellisoft.internationalinstance.db.repso.MetadataJsonRepo;
import com.intellisoft.internationalinstance.service_impl.service.MetadataJsonService;
import com.intellisoft.internationalinstance.util.AppConstants;
import com.intellisoft.internationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MetadataJsonServiceImpl implements MetadataJsonService {

    private final MetadataJsonRepo metadataJsonRepo;

    public void getMetadataData() throws URISyntaxException {

        List<MetadataJson> metadataJsonList = new ArrayList<>();
        var  res = GenericWebclient.getForSingleObjResponse(
                AppConstants.METADATA_JSON_ENDPOINT, String.class);
        JSONObject jsObject = new JSONObject(res);
        JSONArray dataElements = jsObject.getJSONArray("dataElements");
        dataElements.forEach(element -> {

            if (((JSONObject)element).has("code") &&
                    ((JSONObject)element).has("id")){
                String  indicatorId = ((JSONObject)element).getString("id");
                String  code = ((JSONObject)element).getString("code");
                MetadataJson metadataJson = new MetadataJson();
                metadataJson.setId(indicatorId);
                metadataJson.setCode(code);
                metadataJson.setMetadata(element.toString());
                metadataJsonList.add(metadataJson);
            }

        });

        saveMetadataJson(metadataJsonList);

    }

    @Override
    public void saveMetadataJson(List<MetadataJson> metadataJsonList) {

        for (int i = 0; i < metadataJsonList.size(); i++){

            String id = metadataJsonList.get(i).getId();
            String code = metadataJsonList.get(i).getCode();
            String metadata = metadataJsonList.get(i).getMetadata();

            MetadataJson metadataJson = new MetadataJson();
            metadataJson.setId(id);
            metadataJson.setCode(code);
            metadataJson.setMetadata(metadata);
            updateMetadataJson(id, metadataJson);
        }
    }

    @Override
    public List<MetadataJson> listPaginated() {
        return metadataJsonRepo.findAll();
    }

    @Override
    public MetadataJson getMetadataJson(String id) {

        Optional<MetadataJson> optionalMetadataJson =
                metadataJsonRepo.findById(id);
        return optionalMetadataJson.orElse(null);

    }

    @Override
    public MetadataJson getMetadataJsonByCode(String code) {
        Optional<MetadataJson> optionalMetadataJson =
                metadataJsonRepo.findByCode(code);
        return optionalMetadataJson.orElse(null);
    }

    @Override
    public MetadataJson updateMetadataJson(String id, MetadataJson metadataJson) {
        Optional<MetadataJson> optionalMetadataJson =
                metadataJsonRepo.findById(id);
        if (optionalMetadataJson.isPresent()){
            MetadataJson metadataJsonUpdate = optionalMetadataJson.get();
            metadataJsonUpdate.setCode(metadataJson.getCode());
            metadataJsonUpdate.setMetadata(metadataJson.getMetadata());
            return metadataJsonRepo.save(metadataJsonUpdate);
        }
        metadataJson.setId(id);
        return metadataJsonRepo.save(metadataJson);
    }

    @Override
    public void deleteMetadataJson(String id) {
        Optional<MetadataJson> optionalMetadataJson =
                metadataJsonRepo.findById(id);
        if (optionalMetadataJson.isPresent()){
            metadataJsonRepo.deleteById(id);
        }
    }
}
