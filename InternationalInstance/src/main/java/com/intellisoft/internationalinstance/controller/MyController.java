package com.intellisoft.internationalinstance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellisoft.internationalinstance.DbIndicatorDescription;
import com.intellisoft.internationalinstance.DbVersionData;
import com.intellisoft.internationalinstance.FormatterClass;
import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.db.NotificationSubscription;
import com.intellisoft.internationalinstance.db.VersionEntity;
import com.intellisoft.internationalinstance.model.IndicatorForFrontEnd;
import com.intellisoft.internationalinstance.model.Response;
import com.intellisoft.internationalinstance.service_impl.InternationalService;
import com.intellisoft.internationalinstance.service_impl.NotificationService;
import com.intellisoft.internationalinstance.service_impl.VersionService;
import com.intellisoft.internationalinstance.util.GenericWebclient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/master-template")
@RestController
@RequiredArgsConstructor
public class MyController {
    private final VersionService versionService;
    private final NotificationService notificationService;
    FormatterClass formatterClass = new FormatterClass();
    private final InternationalService internationalService;

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

//    @PostMapping("subscribe")
//    public Response subscribe(@RequestBody NotificationSubscription notificationSubscription)  {
//        return notificationService.subscribe(notificationSubscription);
//    }
//    @PutMapping("unsubscribe")
//    public Response unsubscribe(@RequestParam("email") String email)  {
//        return notificationService.unsubscribe(email);
//    }


}
