package com.intellisoft.pssnationalinstance.controller;

import com.intellisoft.pssnationalinstance.DbDataEntryData;
import com.intellisoft.pssnationalinstance.DbVersions;
import com.intellisoft.pssnationalinstance.FormatterClass;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.service_impl.service.DataEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/data-entry")
@RestController
@RequiredArgsConstructor
public class DataEntryController {

    private final DataEntryService dataEntryService;
    private final FormatterClass formatterClass = new FormatterClass();

    /**
     * Save data entry from thr data entry person to the local db
     *
     */
    @PostMapping("/response/save")
    public ResponseEntity<?> addDataEntry(
            @RequestBody DbDataEntryData dbDataEntryData) {

        Results results = dataEntryService.addDataEntry(dbDataEntryData);
        return formatterClass.getResponse(results);

    }

    /**
     * Gets a list of the data entries for this person
     * @param dataEntryPersonId
     * @param limit
     * @param status
     * @param pageNo
     * @return
     */
    @GetMapping("/response")
    public ResponseEntity<?> listDataEntry(
            @RequestParam(value = "dataEntryPersonId", required = true) String dataEntryPersonId,
            @RequestParam(value = "limit", required = false) String limit,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "pageNo", required = false) String pageNo
    ) {

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

        Results results = dataEntryService.listDataEntry(
                pageNumber, limitNo, statusValue, dataEntryPersonId);
        return formatterClass.getResponse(results);
    }

    /**
     * Get Data entry Details
     */
    @GetMapping(value = "/response/{id}")
    public ResponseEntity<?> getDataEntryDetails(@PathVariable("id") String id){
        Results results = dataEntryService.viewDataEntry(id);
        return formatterClass.getResponse(results);
    }

    @PutMapping(value = "/response/{id}")
    public ResponseEntity<?> updateVersions(
            @RequestBody DbDataEntryData dbDataEntryData,
            @PathVariable("id") String id) {
        Results results = dataEntryService.updateDataEntry(id, dbDataEntryData);
        return formatterClass.getResponse(results);
    }

    /**
     * Delete Data entry and all its reposnes
     *
     */
//    @DeleteMapping(value = "/response/{id}")
//    public ResponseEntity<?> deleteDataEntry(@PathVariable("id") Long id){
//        Results results = dataEntryService.deleteDataEntry(id);
//        return formatterClass.getResponse(results);
//    }


}
