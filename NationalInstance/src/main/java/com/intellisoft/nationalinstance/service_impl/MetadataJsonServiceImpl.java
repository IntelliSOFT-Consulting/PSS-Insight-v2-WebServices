package com.intellisoft.nationalinstance.service_impl;

import com.intellisoft.nationalinstance.db.MetadataJson;
import com.intellisoft.nationalinstance.db.repso.MetadataJsonRepo;
import com.intellisoft.nationalinstance.util.AppConstants;
import com.intellisoft.nationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MetadataJsonServiceImpl implements MetadataJsonService{

    private final MetadataJsonRepo metadataJsonRepo;

    @Override
    public void saveMetadataJson(List<MetadataJson> metadataJsonList) {

        for (int i = 0; i < metadataJsonList.size(); i++){

            String id = metadataJsonList.get(i).getId();
            String code = metadataJsonList.get(i).getCode();
            String metadata = metadataJsonList.get(i).getMetadata();

            if (id != null){
                MetadataJson metadataJson = new MetadataJson();
                metadataJson.setId(id);
                metadataJson.setCode(code);
                metadataJson.setMetadata(metadata);

                Optional<MetadataJson> optionalMetadataJson =
                        metadataJsonRepo.findById(id);
                if (optionalMetadataJson.isPresent()){
                    MetadataJson metadataJsonUpdate = optionalMetadataJson.get();
                    metadataJsonUpdate.setCode(metadataJson.getCode());
                    metadataJsonUpdate.setMetadata(metadataJson.getMetadata());
                    metadataJsonRepo.save(metadataJsonUpdate);
                }else {
                    metadataJsonRepo.save(metadataJson);
                }
            }
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
    public void deleteMetadataJson(String id) {
        Optional<MetadataJson> optionalMetadataJson =
                metadataJsonRepo.findById(id);
        if (optionalMetadataJson.isPresent()){
            metadataJsonRepo.deleteById(id);
        }
    }
}
