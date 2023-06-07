package com.intellisoft.pssnationalinstance.controller;

import com.intellisoft.pssnationalinstance.DbIndicatorDetails;
import com.intellisoft.pssnationalinstance.FormatterClass;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.service_impl.service.IndicatorReferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/indicator-reference")
@RestController
@RequiredArgsConstructor
public class IndicatorReferenceController {
    private final IndicatorReferenceService indicatorReference;
    FormatterClass formatterClass = new FormatterClass();

    @Operation(summary = "Add indicator dictionary", description = "Post an indicator dictionary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
                    content = {@Content(examples = {@ExampleObject(value = "")})}),
            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
                    content = {@Content(examples = {@ExampleObject(value = "")})})})
    @PostMapping("/add-indicator-reference")
    public ResponseEntity<?> addIndicatorDictionary(@RequestBody DbIndicatorDetails dbIndicatorDetails) {
        Results results = indicatorReference.addIndicatorDictionary(dbIndicatorDetails);
        return formatterClass.getResponse(results);
    }

    @GetMapping("/list-indicator-reference")
    public ResponseEntity<?> listIndicatorDictionary() {
        Results results = indicatorReference.listIndicatorDictionary();
        return formatterClass.getResponse(results);
    }

    @GetMapping("/list-indicator-reference/{id}")
    public ResponseEntity<?> getIndicatorValues(@PathVariable("id") String id) {
        Results results = indicatorReference.getIndicatorValues(id);
        return formatterClass.getResponse(results);
    }

    @PutMapping(value = "/update-indicator-reference/{id}")
    public ResponseEntity<?> updateVersions(@RequestBody DbIndicatorDetails dbIndicatorDetails, @PathVariable("id") String id) {
        dbIndicatorDetails.setUuid(id);
        Results results = indicatorReference.updateDictionary(dbIndicatorDetails);
        return formatterClass.getResponse(results);
    }

    @DeleteMapping(value = "/delete-indicator-reference/{id}")
    public ResponseEntity<?> deleteDictionary(@PathVariable("id") String id) {
        Results results = indicatorReference.deleteDictionary(id);
        return formatterClass.getResponse(results);
    }

    @GetMapping("/list-topics")
    public ResponseEntity<?> getTopics() {
        Results results = indicatorReference.getTopics();
        return formatterClass.getResponse(results);
    }

}
