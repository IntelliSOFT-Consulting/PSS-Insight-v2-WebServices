package com.intellisoft.nationalinstance.controller;

import com.intellisoft.nationalinstance.DbVersionData;
import com.intellisoft.nationalinstance.FormatterClass;
import com.intellisoft.nationalinstance.Results;
import com.intellisoft.nationalinstance.db.IndicatorDescription;
import com.intellisoft.nationalinstance.db.VersionEntity;
import com.intellisoft.nationalinstance.model.Response;
import com.intellisoft.nationalinstance.service_impl.IndicatorDescriptionService;
import com.intellisoft.nationalinstance.service_impl.VersionService;
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


    private final VersionService versionService;
    FormatterClass formatterClass = new FormatterClass();


    /**
     * Save versions to local and Datastore
     * @param dbVersionData
     * @return
     * @throws URISyntaxException
     */
    @PostMapping("/version")
    public VersionEntity createVersion(
            @RequestBody DbVersionData dbVersionData) throws URISyntaxException {
        return versionService.saveDraftOrPublish(dbVersionData);
    }

    /**
     * Update either local or data store
     */
    @PutMapping(value = "/version/{versionId}")
    public VersionEntity updateVersions(
            @RequestBody DbVersionData dbVersionData,
            @PathVariable("versionId") Long versionId)throws URISyntaxException{

        dbVersionData.setVersionId(versionId);
        return versionService.saveDraftOrPublish(dbVersionData);

    }

    /**
     * Get Version Details
     */
    @GetMapping(value = "/version/{versionId}")
    public ResponseEntity<?> getVersionDetails(@PathVariable("versionId") String versionId){
        Results results = versionService.getVersion(versionId);
        return formatterClass.getResponse(results);

    }
    /**
     * Get Published indicators
     */
    @GetMapping(value = "/published-indicators")
    public ResponseEntity<?> getPublishedIndicators() throws URISyntaxException {
        Results results = versionService.getPublishedIndicators();
        return formatterClass.getResponse(results);

    }

    /**
     * Get Data from local database
     */
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

    /**
     * Delete a template
     */
    @DeleteMapping(value = "/version/{versionId}")
    public ResponseEntity<?> deleteTemplate(@PathVariable("versionId") long versionId) {
        Results results = versionService.deleteTemplate(versionId);
        return formatterClass.getResponse(results);
    }







}
