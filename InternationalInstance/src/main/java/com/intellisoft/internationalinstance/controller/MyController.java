package com.intellisoft.internationalinstance.controller;

import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.service_impl.impl.InternationalServiceImpl;
import com.intellisoft.internationalinstance.service_impl.service.InternationalService;
import com.intellisoft.internationalinstance.service_impl.service.JavaMailSenderService;
import com.intellisoft.internationalinstance.service_impl.service.NotificationService;
import com.intellisoft.internationalinstance.service_impl.service.VersionService;
import com.intellisoft.internationalinstance.util.AppConstants;
import com.itextpdf.text.*;

import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/master-template")
@RestController
@RequiredArgsConstructor
public class MyController {
    private final VersionService versionService;
    FormatterClass formatterClass = new FormatterClass();
    private final InternationalService internationalService;
    private final JavaMailSenderService javaMailSenderService;
    private RestTemplate restTemplate = new RestTemplate();

    @Operation(
            summary = "Pull the international indicators from the metadata json ",
            description = "This api is used for pulling the international indicators from the datastore")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @GetMapping("/indicators")
    public ResponseEntity<?> getIndicatorForFrontEnd() {

        Results results = internationalService.getIndicators();
        return formatterClass.getResponse(results);
    }

    @Operation(
            summary = "Create a version using the provided indicators",
            description = "Post a version indicating if its a draft or a published version")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @PostMapping("/version")
    public ResponseEntity<?> createVersion(
            @RequestBody DbVersionData dbVersionData) {

        Results results = internationalService.saveUpdate(dbVersionData);
        return formatterClass.getResponse(results);
    }

    @Operation(
            summary = "Update version details.",
            description = "You cannot update a published version")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @PutMapping(value = "/version/{versionId}")
    public ResponseEntity<?> updateVersions(
            @RequestBody DbVersionData dbVersionData,
            @PathVariable("versionId") String versionId ){

        dbVersionData.setVersionId(Long.valueOf(versionId));

        Results results = internationalService.saveUpdate(dbVersionData);
        return formatterClass.getResponse(results);


    }

    @Operation(
            summary = "Version details ",
            description = "The api provides the version details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @GetMapping(value = "/version/{versionId}")
    public ResponseEntity<?> getVersionDetails(@PathVariable("versionId") Long versionId){
        Results results = versionService.getVersion(versionId);
        return formatterClass.getResponse(results);

    }

    @Operation(
            summary = "Pull the saved versions.",
            description = "This api receives two statuses: DRAFT & PUBLISHED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @GetMapping(value = "/version")
    public ResponseEntity<?> getTemplates(
            @RequestParam(value = "limit", required = false) String limit,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pageNo", required = false) String pageNo
    ){

        int limitNo = 10;
        if (limit != null && !limit.equals("")){
            limitNo = Integer.parseInt(limit);
        }
        String statusValue = "ALL";
        if (status != null && !status.equals("")){
            statusValue = status;
        }
        int pageNumber = 1;
        if (pageNo != null && !pageNo.equals("")){
            pageNumber = Integer.parseInt(pageNo);
        }

        Results results = versionService.getTemplates(pageNumber, limitNo, statusValue);
        return formatterClass.getResponse(results);

    }

    @Operation(
            summary = "Deletes a version ",
            description = "A published version cannot be deleted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
    @DeleteMapping(value = "/version/{versionId}")
    public ResponseEntity<?> deleteTemplate(@PathVariable("versionId") long versionId) {
        Results results = versionService.deleteTemplate(versionId);
        return formatterClass.getResponse(results);
    }


    @GetMapping("/view-file/{filename}")
    public ResponseEntity<byte[]> getDocument(@PathVariable String filename) {
        String url = AppConstants.DOCS_ENDPOINT + filename + "/data";
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "district");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(url,
                HttpMethod.GET,
                entity, byte[].class);
        return ResponseEntity
                .status(response.getStatusCode())
                .contentType(MediaType.APPLICATION_PDF)
                .body(response.getBody());
    }


}
