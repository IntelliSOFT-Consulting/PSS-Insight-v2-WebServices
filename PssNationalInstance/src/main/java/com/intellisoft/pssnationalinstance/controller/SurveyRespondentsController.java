package com.intellisoft.pssnationalinstance.controller;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.service_impl.service.SurveyRespondentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/survey-respondents")
@RestController
@RequiredArgsConstructor
public class SurveyRespondentsController {

    private final SurveyRespondentsService surveyRespondentsService;
    FormatterClass formatterClass = new FormatterClass();


    @PostMapping("/add")
    public ResponseEntity<?> addSurveyRespondent(
            @RequestBody DbSurveyRespondent dbSurveyRespondent) {

        Results results = surveyRespondentsService
                .addSurveyRespondent(dbSurveyRespondent);

        return formatterClass.getResponse(results);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteRespondent(
            @PathVariable("id") String id) {
        Results results = surveyRespondentsService
                .deleteRespondent(id);
        return formatterClass.getResponse(results);
    }

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(
            @RequestBody DbVerifySurvey dbVerifySurvey){
        Results results = surveyRespondentsService
                .verifyPassword(dbVerifySurvey);
        return formatterClass.getResponse(results);
    }

    @PostMapping("/response/save")
    public ResponseEntity<?> saveResponse(
            @RequestBody DbResponse dbResponse){
        Results results = surveyRespondentsService
                .saveResponse(dbResponse);
        return formatterClass.getResponse(results);
    }

    @GetMapping(value = "/details/{respondentId}")
    public ResponseEntity<?> getRespondentDetails(
            @PathVariable("respondentId") String respondentId,
            @RequestParam(value = "questions", required = false) String questions,
            @RequestParam(value = "responses", required = false) String responses,
            @RequestParam(value = "respondentDetails", required = false) String respondentDetails
    ) throws URISyntaxException {
        Results results = surveyRespondentsService
                .getRespondentDetails(
                        respondentId,
                        questions,
                        responses,
                        respondentDetails);
        return formatterClass.getResponse(results);

    }
    @PostMapping("/resend-survey/{respondentId}")
    public ResponseEntity<?> resendSurvey(
            @PathVariable("respondentId") String respondentId,
            @RequestBody DbResendSurvey resendSurvey){
        Results results = surveyRespondentsService
                .resendSurvey(respondentId,resendSurvey);
        return formatterClass.getResponse(results);
    }

    @PostMapping("/request-link/{respondentId}")
    public ResponseEntity<?> requestLink(
            @PathVariable("respondentId") String respondentId,
            @RequestBody DbRequestLink dbRequestLink){
        Results results = surveyRespondentsService
                .requestLink(respondentId,dbRequestLink);
        return formatterClass.getResponse(results);
    }


    // new end-points to implement confirmation & rejection of survey responses::
    @PutMapping("/{respondentId}/verify-survey")
    public ResponseEntity<Results> confirmSurvey(@PathVariable String respondentId){
        return ResponseEntity.status(HttpStatus.OK).body(surveyRespondentsService.verifySurvey(respondentId));
    }

    @PutMapping("/{respondentId}/reject-survey")
    public ResponseEntity<Results> rejectSurvey(@PathVariable String respondentId){
        return ResponseEntity.status(HttpStatus.OK).body(surveyRespondentsService.rejectSurvey(respondentId));
    }


}
