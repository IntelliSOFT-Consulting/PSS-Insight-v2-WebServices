package com.intellisoft.pssnationalinstance.controller;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.service_impl.service.DataEntryService;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/file")
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FormatterClass formatterClass = new FormatterClass();

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file) {

        try{
            RestTemplate restTemplate = new RestTemplate();
            String authHeader = "Basic " + Base64.getEncoder().encodeToString((
                    "admin" + ":" + "district").getBytes());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.add("Authorization", authHeader);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);
            ResponseEntity<DbFileResources> responseEntity =
                    restTemplate.postForEntity(
                            AppConstants.FILES_RESOURCES_ENDPOINT,
                            requestEntity, DbFileResources.class);

            int code = responseEntity.getStatusCodeValue();
            if (code == 202){
                if (responseEntity.getBody() != null){
                    if (responseEntity.getBody().getResponse() != null){
                        if (responseEntity.getBody().getResponse().getFileResource() != null){
                            String id = responseEntity.getBody().getResponse().getFileResource().getId();
                            Results results = new Results(200, new DbResFileRes(id));
                            return formatterClass.getResponse(results);
                        }
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        Results results = new Results(400, "Could not upload files");
        return formatterClass.getResponse(results);
    }


}
