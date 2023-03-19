package com.intellisoft.nationalinstance.controller;

import com.intellisoft.nationalinstance.DbSurvey;
import com.intellisoft.nationalinstance.DbSurveyRespondent;
import com.intellisoft.nationalinstance.FormatterClass;
import com.intellisoft.nationalinstance.Results;
import com.intellisoft.nationalinstance.service_impl.SurveysService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/survey")
@RestController
@RequiredArgsConstructor
public class SurveyController {

    private final SurveysService surveysService;
    FormatterClass formatterClass = new FormatterClass();

    /**
     * Add survey data
     * @param dbSurvey
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity<?> addSurvey(
            @RequestBody DbSurvey dbSurvey){
        Results results = surveysService
                .addSurvey(dbSurvey);
        return formatterClass.getResponse(results);
    }

    /**
     * List saved surveys by a particular user
     * @return
     */
    @GetMapping(value = "/admin-surveys/{creatorId}")
    public ResponseEntity<?> listAdminSurvey(
            @PathVariable("creatorId") String creatorId,
            @RequestParam("status") String status

    ){
        Results results = surveysService
                .listAdminSurveys(creatorId,status);
        return formatterClass.getResponse(results);

    }
    @GetMapping(value = "/admin-respondents/{creatorId}")
    public ResponseEntity<?> listSurvey(
            @PathVariable("creatorId") String creatorId,
            @RequestParam("status") String status
    ){
        Results results = surveysService
                .listRespondentsSurveys(creatorId, status);
        return formatterClass.getResponse(results);

    }
    @GetMapping(value = "/{surveyId}")
    public ResponseEntity<?> surveyDetails(
            @PathVariable("surveyId") String surveyId){
        Results results = surveysService
                .surveyDetails(surveyId);
        return formatterClass.getResponse(results);

    }
}
