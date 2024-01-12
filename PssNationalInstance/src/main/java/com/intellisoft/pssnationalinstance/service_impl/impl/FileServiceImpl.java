package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.service_impl.service.FileService;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Log4j2
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${dhis.username}")
    private String username;
    @Value("${dhis.password}")
    private String password;


    @Override
    public Results createFileResource(MultipartFile file) {

        try {

            RestTemplate restTemplate = new RestTemplate();
            String authHeader = "Basic " + Base64.getEncoder().encodeToString((
                    username + ":" + password).getBytes());
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
                            if (id != null){
                                String documentId = getDocumentDetails(id);
                                String documentUrl = AppConstants.DOCUMENT_RESOURCES_ENDPOINT + documentId + "/data";
                                return new Results(200, new DbResFileRes(documentUrl));
                            }

                        }
                    }
                }
            }


        }catch (Exception e){
            log.error("An error occurred during file processing");
        }

        return new Results(400, "There was an issue with this request. Please try again.");
    }

    @Override
    public String getDocumentDetails(String fileId){

        try{
            DbDocuments dbDocuments = new DbDocuments(
                    fileId,
                    "UPLOAD_FILE",
                    true,
                    false,
                    fileId
            );

            DbDocumentFile dbDocumentFile = GenericWebclient.postForSingleObjResponse(
                    AppConstants.NATIONAL_BASE_DOCUMENT,
                    dbDocuments,
                    DbDocuments.class,
                    DbDocumentFile.class);

            if (dbDocumentFile != null){

                DbFileResponse response = dbDocumentFile.getResponse();
                if (response != null){
                    String uuid = response.getUid();
                    if (uuid != null){
                        return uuid;
                    }
                }

            }

        }catch (Exception e){
            log.error("An error occurred when fetching document details");
        }
        return null;

    }

    @Override
    public ResponseEntity<Resource> getDocument(String documentId) {

        try{

            String id = getDocumentDetails(documentId);

            RestTemplate restTemplate = new RestTemplate();
            String authHeader = "Basic " + Base64.getEncoder().encodeToString((
                    username + ":" + password).getBytes());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", authHeader);
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<Resource> response = restTemplate.exchange(
                    AppConstants.NATIONAL_BASE_DOCUMENT + "/" + id + "/data",
                    HttpMethod.GET, entity, Resource.class);



            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + id)
                    .contentLength(response.getBody().contentLength())
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(response.getBody());

        }catch (Exception e){
            log.error("An error occurred when fetching the document");
        }

        return null;
    }
}
