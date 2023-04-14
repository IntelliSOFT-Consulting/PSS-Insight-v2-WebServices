package com.intellisoft.pssnationalinstance.controller;


import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.service_impl.service.*;
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
@RequestMapping(value = "/api/v1/configuration/")
@RestController
@RequiredArgsConstructor
public class PeriodConfigurationController {

    private final PeriodConfigurationService periodConfigurationService;
    private final AboutUsService aboutUsService;

    private final FormatterClass formatterClass = new FormatterClass();


    @PostMapping("create-period")
    public ResponseEntity<?> addPeriodConfiguration(
            @RequestBody DbPeriodConfiguration dbPeriodConfiguration){
        Results results = periodConfigurationService.addPeriodConfiguration(dbPeriodConfiguration);
        return formatterClass.getResponse(results);
    }


    @GetMapping(value = "list-period")
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

    @PutMapping(value = "update-period/{id}")
    public ResponseEntity<?> updatePeriodConfiguration(
            @RequestBody DbPeriodConfiguration dbPeriodConfiguration,
            @PathVariable("id") String id) {
        Results results = periodConfigurationService
                .updatePeriodConfiguration(id, dbPeriodConfiguration);
        return formatterClass.getResponse(results);
    }

    //Add About us text
    @PostMapping("create-about-us")
    public ResponseEntity<?> addAboutUs(
            @RequestBody DbAboutUs dbAboutUs){
        Results results = aboutUsService.addAboutUs(dbAboutUs);
        return formatterClass.getResponse(results);
    }

    @GetMapping(value = "list-about-us")
    public ResponseEntity<?> listAboutUs(
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "isLatest", required = false) String isLatest

    ){
        int pageNo = 1;
        int sizeNo = 10;
        if (page != null)
            pageNo = Integer.parseInt(page);
        if (size != null)
            sizeNo = Integer.parseInt(size);

        boolean isLatestValue = false;
        if (isLatest != null){
            if (isLatest.equals("true")){
                isLatestValue = true;
            }
        }

        Results results = aboutUsService
                .listAboutUs(isLatestValue, pageNo,sizeNo);
        return formatterClass.getResponse(results);

    }

    @GetMapping(value = "list-about-us/{id}")
    public ResponseEntity<?> aboutUsDetails(
            @PathVariable("id") String id
    ){


        Results results = aboutUsService.aboutUsDetails(id);
        return formatterClass.getResponse(results);

    }
    @PutMapping(value = "update-about-us/{id}")
    public ResponseEntity<?> updateAboutUs(
            @RequestBody DbAboutUs dbAboutUs,
            @PathVariable("id") String id) {
        Results results = aboutUsService
                .updateAboutUs(id, dbAboutUs);
        return formatterClass.getResponse(results);
    }

}
