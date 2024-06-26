package com.intellisoft.pssnationalinstance.controller;


import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.service_impl.service.*;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import com.intellisoft.pssnationalinstance.util.EnvUrlConstants;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/national-template/")
@RestController
@RequiredArgsConstructor
public class NationalTemplateController {

    private final NationalTemplateService nationalTemplateService;
    private final FormatterClass formatterClass = new FormatterClass();
    private final IndicatorEditsService indicatorEditsService;
    private final VersionEntityService versionEntityService;
    private final PeriodConfigurationService periodConfigurationService;
    private final EnvUrlConstants envUrlConstants;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${dhis.username}")
    private String username;
    @Value("${dhis.password}")
    private String password;


    /**
     * Update the national instance with the international data from the international data
     *
     * @return
     * @throws URISyntaxException
     */
    @Operation(summary = "Get the national org units", description = "This api is used for pulling the national org units")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"), @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}", content = {@Content(examples = {@ExampleObject(value = "")})}), @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}", content = {@Content(examples = {@ExampleObject(value = "")})})})
    @GetMapping("organisation-units")
    public ResponseEntity<?> getOrgUnits(@RequestParam(value = "page", required = false) String page) {
        int pageNo = 1;
        if (page != null) pageNo = Integer.parseInt(page);

        Results results = nationalTemplateService.getOrgUnits(pageNo);
        return formatterClass.getResponse(results);
    }

    @Operation(summary = "Pull the international template", description = "This api is used for pulling the international template and displaying it to frontend")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"), @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}", content = {@Content(examples = {@ExampleObject(value = "")})}), @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}", content = {@Content(examples = {@ExampleObject(value = "")})})})
    @GetMapping("published-indicators")
    public ResponseEntity<?> getNationalIndicators() {
        Results results = nationalTemplateService.getNationalPublishedVersion();
        return formatterClass.getResponse(results);
    }

    /**
     * Update the national instance with the international data from the international data
     *
     * @return
     * @throws URISyntaxException
     */
    @Operation(summary = "Get indicator description", description = "This api is used for getting indicator descriptions")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"), @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}", content = {@Content(examples = {@ExampleObject(value = "")})}), @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}", content = {@Content(examples = {@ExampleObject(value = "")})})})
    @GetMapping("indicator-description/{code}")
    public ResponseEntity<?> getIndicatorDescription(@PathVariable("code") String code) {
        Results results = nationalTemplateService.getIndicatorDescription(code);
        return formatterClass.getResponse(results);
    }

    @PostMapping("edit-indicator")
    public ResponseEntity<?> addEdit(@RequestBody DbIndicatorEdit dbIndicatorEdit) {
        Results results = indicatorEditsService.addEdit(dbIndicatorEdit);
        return formatterClass.getResponse(results);
    }


    @GetMapping(value = "list-versions")
    public ResponseEntity<?> listVersions(@RequestParam(value = "page", required = false) String page, @RequestParam(value = "size", required = false) String size, @RequestParam(value = "isLatest", required = false) String isLatest

    ) {
        int pageNo = 1;
        int sizeNo = 10;
        boolean isLatestTemplate = false;
        if (page != null) pageNo = Integer.parseInt(page);
        if (size != null) sizeNo = Integer.parseInt(size);
        if (isLatest != null) isLatestTemplate = Boolean.parseBoolean(isLatest);

        Results results = versionEntityService.listVersions(pageNo, sizeNo, isLatestTemplate);
        return formatterClass.getResponse(results);

    }

    @PutMapping(value = "version-details/{id}")
    public ResponseEntity<?> updateVersions(@RequestBody DbVersions dbVersionData, @PathVariable("id") String id) {
        Results results = versionEntityService.updateVersion(id, dbVersionData);
        return formatterClass.getResponse(results);
    }

    @GetMapping(value = "version-details/{id}")
    public ResponseEntity<?> getVersionDetails(@PathVariable("id") String id) {
        Results results = versionEntityService.getVersionDetails(id);
        return formatterClass.getResponse(results);
    }

    @PostMapping("add-version")
    public ResponseEntity<?> addVersion(@RequestBody DbVersions dbVersions) {
        Results results = versionEntityService.addVersion(dbVersions);
        return formatterClass.getResponse(results);
    }

    @Operation(summary = "Deletes a version ", description = "A published version cannot be deleted.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"), @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}", content = {@Content(examples = {@ExampleObject(value = "")})}), @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}", content = {@Content(examples = {@ExampleObject(value = "")})})})
    @DeleteMapping(value = "/version-details/{versionId}")
    public ResponseEntity<?> deleteTemplate(@PathVariable("versionId") String versionId) {
        Results results = versionEntityService.deleteTemplate(versionId);
        return formatterClass.getResponse(results);
    }

    @Operation(summary = "Pull the international template", description = "This api is used for pulling the international template and displaying it to frontend")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"), @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}", content = {@Content(examples = {@ExampleObject(value = "")})}), @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}", content = {@Content(examples = {@ExampleObject(value = "")})})})
    @GetMapping("details")
    public ResponseEntity<?> getNationalDetails() {
        Results results = nationalTemplateService.getNationalDetails();
        return formatterClass.getResponse(results);
    }

    @GetMapping("/view-file/{filename}")
    public ResponseEntity<byte[]> getDocument(@PathVariable String filename) {

        String url = envUrlConstants.getINTERNATIONAL_DOCS_ENDPOINT()+ filename + "/data";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        return ResponseEntity.status(response.getStatusCode()).contentType(MediaType.APPLICATION_PDF).body(response.getBody());
    }


}
