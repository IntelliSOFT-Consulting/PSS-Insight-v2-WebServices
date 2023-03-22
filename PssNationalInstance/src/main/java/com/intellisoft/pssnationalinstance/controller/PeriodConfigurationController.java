package com.intellisoft.pssnationalinstance.controller;


import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.service_impl.service.IndicatorEditsService;
import com.intellisoft.pssnationalinstance.service_impl.service.NationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.PeriodConfigurationService;
import com.intellisoft.pssnationalinstance.service_impl.service.VersionEntityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/period-configuration/")
@RestController
@RequiredArgsConstructor
public class PeriodConfigurationController {

    private final PeriodConfigurationService periodConfigurationService;

    private final FormatterClass formatterClass = new FormatterClass();


    @PostMapping("create")
    public ResponseEntity<?> addPeriodConfiguration(
            @RequestBody DbPeriodConfiguration dbPeriodConfiguration){
        Results results = periodConfigurationService.addPeriodConfiguration(dbPeriodConfiguration);
        return formatterClass.getResponse(results);
    }


    @GetMapping(value = "list-configuration")
    public ResponseEntity<?> listVersions(
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "size", required = false) String size

    ){
        int pageNo = 1;
        int sizeNo = 10;
        if (page != null)
            pageNo = Integer.parseInt(page);
        if (size != null)
            sizeNo = Integer.parseInt(size);
        Results results = periodConfigurationService
                .listPeriodConfiguration(pageNo,sizeNo);
        return formatterClass.getResponse(results);

    }

    @PutMapping(value = "update-configuration/{id}")
    public ResponseEntity<?> updatePeriodConfiguration(
            @RequestBody DbPeriodConfiguration dbPeriodConfiguration,
            @PathVariable("id") String id) {
        Results results = periodConfigurationService
                .updatePeriodConfiguration(id, dbPeriodConfiguration);
        return formatterClass.getResponse(results);
    }


}
