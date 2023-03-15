package com.intellisoft.internationalinstance.controller;

import com.intellisoft.internationalinstance.DbVersionData;
import com.intellisoft.internationalinstance.FormatterClass;
import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.db.NotificationSubscription;
import com.intellisoft.internationalinstance.db.VersionEntity;
import com.intellisoft.internationalinstance.model.IndicatorForFrontEnd;
import com.intellisoft.internationalinstance.model.Response;
import com.intellisoft.internationalinstance.service_impl.NotificationService;
import com.intellisoft.internationalinstance.service_impl.VersionService;
import lombok.RequiredArgsConstructor;
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

    /**
     * Pull all the indicators from the international data store and display to frontend
     * @return
     * @throws URISyntaxException
     */
    @GetMapping("/indicators")
    public ResponseEntity<?> getIndicatorForFrontEnd() throws URISyntaxException {
        Results results = versionService.getIndicators();
        return formatterClass.getResponse(results);
    }
    @PostMapping("subscribe")
    public Response subscribe(@RequestBody NotificationSubscription notificationSubscription)  {
        return notificationService.subscribe(notificationSubscription);
    }
    @PutMapping("unsubscribe")
    public Response unsubscribe(@RequestParam("email") String email)  {
        return notificationService.unsubscribe(email);
    }


    /**
     * Save versions to local and Datastore
     * @param dbVersionData
     * @return
     * @throws URISyntaxException
     */
    @PostMapping("/version")
    public ResponseEntity<?> createVersion(
            @RequestBody DbVersionData dbVersionData) throws URISyntaxException {
        Results results = versionService.saveDraftOrPublish(dbVersionData);
        return formatterClass.getResponse(results);
    }

    /**
     * Update either local or data store
     */
    @PutMapping(value = "/version/{versionId}")
    public ResponseEntity<?> updateVersions(
            @RequestBody DbVersionData dbVersionData,
            @Param("versionId") Long versionId)throws URISyntaxException{

        dbVersionData.setVersionId(versionId);

        Results results = versionService.saveDraftOrPublish(dbVersionData);
        return formatterClass.getResponse(results);


    }

    /**
     * Get Version Details
     */
    @GetMapping(value = "/version/{versionId}")
    public ResponseEntity<?> getVersionDetails(@PathVariable("versionId") Long versionId){
        Results results = versionService.getVersion(versionId);
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
