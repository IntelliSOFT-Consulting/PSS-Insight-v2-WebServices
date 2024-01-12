package com.intellisoft.pssnationalinstance.controller;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.service_impl.service.DataEntryService;
import com.intellisoft.pssnationalinstance.service_impl.service.FileService;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Log4j2
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/file")
@RestController
@RequiredArgsConstructor
public class FileController {

    private RestTemplate restTemplate = new RestTemplate();
    private final FileService fileService;
    private final FormatterClass formatterClass = new FormatterClass();

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file) {

        try {
            Results results = fileService.createFileResource(file);
            return formatterClass.getResponse(results);
        }catch (Exception e){
            log.error("An error occurred during file processing");
        }
        Results results = new Results(400, "Check on the request again");
        return formatterClass.getResponse(results);
    }

    @GetMapping("/view-file/{fileId}")
    public ResponseEntity<Resource> displayRemoteFile(@PathVariable String fileId) {
        return fileService.getDocument(fileId);
    }


}
