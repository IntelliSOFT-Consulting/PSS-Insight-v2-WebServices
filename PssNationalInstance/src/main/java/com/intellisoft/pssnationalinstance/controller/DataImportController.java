package com.intellisoft.pssnationalinstance.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.service_impl.service.DataEntryService;
import com.intellisoft.pssnationalinstance.service_impl.service.DataImportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URISyntaxException;

import static org.springframework.http.MediaType.ALL_VALUE;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/data-import")
@RestController
@RequiredArgsConstructor
public class DataImportController {

    @Autowired
    DataImportService dataImportService;

    @Operation(summary = "Post Data import")
    @PostMapping(path = "/import", consumes = ALL_VALUE)
    public ResponseEntity<Results> postDataImport(@Valid @RequestBody DbDataImport dbDataImport) throws URISyntaxException, JsonProcessingException {
        return ResponseEntity.status(HttpStatus.OK).body(dataImportService.postDataImport(dbDataImport));
    }
}
