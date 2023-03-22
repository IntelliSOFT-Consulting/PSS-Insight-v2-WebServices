package com.intellisoft.pssnationalinstance.controller;


import com.intellisoft.pssnationalinstance.DbIndicatorEdit;
import com.intellisoft.pssnationalinstance.DbVersions;
import com.intellisoft.pssnationalinstance.FormatterClass;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.service_impl.service.IndicatorEditsService;
import com.intellisoft.pssnationalinstance.service_impl.service.InternationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.NationalTemplateService;
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
@RequestMapping(value = "/api/v1/national-template/")
@RestController
@RequiredArgsConstructor
public class NationalTemplateController {

    private final NationalTemplateService nationalTemplateService;
    private final FormatterClass formatterClass = new FormatterClass();
    private final IndicatorEditsService indicatorEditsService;
    private final VersionEntityService versionEntityService;


    /**
     * Update the national instance with the international data from the international data
     * @return
     * @throws URISyntaxException
     */
    @Operation(
            summary = "Pull the international template",
            description = "This api is used for pulling the international template and displaying it to frontend")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @GetMapping("published-indicators")
    public ResponseEntity<?> getNationalIndicators() {
        Results results = nationalTemplateService.getNationalPublishedVersion();
        return formatterClass.getResponse(results);
    }

    /**
     * Update the national instance with the international data from the international data
     * @return
     * @throws URISyntaxException
     */
    @Operation(
            summary = "Get indicator description",
            description = "This api is used for getting indicator descriptions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @GetMapping("indicator-description/{code}")
    public ResponseEntity<?> getIndicatorDescription(@PathVariable("code") String code) {
        Results results = nationalTemplateService.getIndicatorDescription(code);
        return formatterClass.getResponse(results);
    }

    @PostMapping("edit-indicator")
    public ResponseEntity<?> addEdit(
            @RequestBody DbIndicatorEdit dbIndicatorEdit){
        Results results = indicatorEditsService.addEdit(dbIndicatorEdit);
        return formatterClass.getResponse(results);
    }
    @PostMapping("add-version")
    public ResponseEntity<?> addVersion(
            @RequestBody DbVersions dbVersions){
        Results results = versionEntityService.addVersion(dbVersions);
        return formatterClass.getResponse(results);
    }

    @GetMapping(value = "list-versions")
    public ResponseEntity<?> listVersions(
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "isLatest", required = false) String isLatest

    ){
        int pageNo = 1;
        int sizeNo = 10;
        boolean isLatestTemplate = false;
        if (page != null)
            pageNo = Integer.parseInt(page);
        if (size != null)
            sizeNo = Integer.parseInt(size);
        if (isLatest != null)
            isLatestTemplate = Boolean.parseBoolean(isLatest);

        Results results = versionEntityService
                .listVersions(pageNo,sizeNo, isLatestTemplate);
        return formatterClass.getResponse(results);

    }

    @PutMapping(value = "version-details/{id}")
    public ResponseEntity<?> updateVersions(
            @RequestBody DbVersions dbVersionData,
            @PathVariable("id") String id) {
        Results results = versionEntityService
                .updateVersion(id, dbVersionData);
        return formatterClass.getResponse(results);

    }




}
