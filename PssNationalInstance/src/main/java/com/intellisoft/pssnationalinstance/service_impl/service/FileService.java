package com.intellisoft.pssnationalinstance.service_impl.service;

import com.intellisoft.pssnationalinstance.Results;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    Results createFileResource(MultipartFile file);
    ResponseEntity<Resource> getDocument(String documentId);

    String getDocumentDetails(String id);
}
