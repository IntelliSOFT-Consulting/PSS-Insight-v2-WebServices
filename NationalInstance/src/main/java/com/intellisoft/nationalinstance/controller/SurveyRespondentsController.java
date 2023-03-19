package com.intellisoft.nationalinstance.controller;

import com.intellisoft.nationalinstance.*;
import com.intellisoft.nationalinstance.db.VersionEntity;
import com.intellisoft.nationalinstance.service_impl.SurveyRespondentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/survey-respondents")
@RestController
@RequiredArgsConstructor
public class SurveyRespondentsController {

    private final SurveyRespondentsService surveyRespondentsService;
    FormatterClass formatterClass = new FormatterClass();

    @PostMapping("/add")
    public ResponseEntity<?> addSurveyRespondent(
            @RequestBody DbSurveyRespondent dbSurveyRespondent) throws UnknownHostException {
        Results results = surveyRespondentsService
                .addSurveyRespondent(dbSurveyRespondent);

        return formatterClass.getResponse(results);
    }

    @GetMapping(value = "/{surveyId}")
    public ResponseEntity<?> listSurveyRespondent(
            @PathVariable("surveyId") String surveyId){
        Results results = surveyRespondentsService
                .listSurveyRespondent(surveyId);
        return formatterClass.getResponse(results);

    }

    @DeleteMapping(value = "/survey/{surveyId}")
    public ResponseEntity<?> deleteSurveyRespondent(
            @PathVariable("surveyId") String surveyId) {
        Results results = surveyRespondentsService
                .deleteSurveyRespondent(surveyId);
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
            @RequestBody DbSurveyDetails dbSurveyDetails){
        Results results = surveyRespondentsService
                .verifyPassword(dbSurveyDetails);
        return formatterClass.getResponse(results);
    }

    @GetMapping(value = "/questions/{respondentId}")
    public ResponseEntity<?> getAssignedSurvey(
            @PathVariable("respondentId") String respondentId){
        Results results = surveyRespondentsService
                .getAssignedSurvey(respondentId);
        return formatterClass.getResponse(results);

    }
    @PostMapping("/response/save")
    public ResponseEntity<?> saveResponse(
            @RequestBody DbResponse dbResponse){
        Results results = surveyRespondentsService
                .saveResponse(dbResponse);
        return formatterClass.getResponse(results);
    }
    @PostMapping("/response/request-link")
    public ResponseEntity<?> requestLink(
            @RequestBody DbRequestLink dbRequestLink){
        Results results = surveyRespondentsService
                .requestLink(dbRequestLink);
        return formatterClass.getResponse(results);
    }
    @GetMapping(value = "/answers/{respondentId}")
    public ResponseEntity<?> getAnswers(
            @PathVariable("respondentId") String respondentId){
        Results results = surveyRespondentsService
                .getAssignedAnswers(respondentId);
        return formatterClass.getResponse(results);

    }

//    @PutMapping(value = "/{surveyId}")
//    public VersionEntity updateVersions(
//            @RequestBody DbVersionData dbVersionData,
//            @PathVariable("surveyId") String surveyId)throws URISyntaxException {
//
//        dbVersionData.setVersionId(surveyId);
//        return versionService.saveDraftOrPublish(dbVersionData);
//
//    }

}
