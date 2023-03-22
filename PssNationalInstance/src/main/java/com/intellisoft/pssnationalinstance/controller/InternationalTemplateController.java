package com.intellisoft.pssnationalinstance.controller;


import com.intellisoft.pssnationalinstance.FormatterClass;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.service_impl.service.InternationalTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/international-template/")
@RestController
@RequiredArgsConstructor
public class InternationalTemplateController {

    private final InternationalTemplateService internationalService;
    private final FormatterClass formatterClass = new FormatterClass();


    /**
     * TODO: Pull the international template and add WITH the national template
     */

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
    @GetMapping("indicators")
    public ResponseEntity<?> getInternationalIndicators() {
        Results results = internationalService.getInternationalIndicators();
        return formatterClass.getResponse(results);
    }





//    /**
//     * Pull the saved national template and have both international template and locally saved template
//     * @return
//     * @throws URISyntaxException
//     */
//    @Operation(
//            summary = "Pull the international template and update the national template",
//            description = "This api is used for pulling the national template.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "${api.response-codes.ok.desc}"),
//            @ApiResponse(responseCode = "400", description = "${api.response-codes.badRequest.desc}",
//                    content = { @Content(examples = { @ExampleObject(value = "") }) }),
//            @ApiResponse(responseCode = "404", description = "${api.response-codes.notFound.desc}",
//                    content = { @Content(examples = { @ExampleObject(value = "") }) }) })
//    @GetMapping("sync")
//    public Response getMasterTemplate() throws URISyntaxException {
//        return versionService.syncVersion();
//    }





}
