package com.intellisoft.pssnationalinstance.controller;

import com.intellisoft.pssnationalinstance.DbSurvey;
import com.intellisoft.pssnationalinstance.DbVersions;
import com.intellisoft.pssnationalinstance.FormatterClass;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.service_impl.service.SurveysService;
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
    @GetMapping(value = "/list/{creatorId}")
    public ResponseEntity<?> listAdminSurvey(
            @PathVariable("creatorId") String creatorId,
            @RequestParam(value = "status", required = false) String status

    ){

        if (status == null || status.equals("")){
            status = "ALL";
        }

        Results results = surveysService
                .listAdminSurveys(creatorId,status);
        return formatterClass.getResponse(results);

    }

    @GetMapping(value = "/survey-details/{surveyId}")
    public ResponseEntity<?> surveyDetails(
            @PathVariable("surveyId") String surveyId,
            @RequestParam(value = "isRespondents", required = false) String respondents

    ){
        boolean isRespondents = false;
        if (respondents != null && !respondents.equals("") && !respondents.equals("false")){
            isRespondents = true;
        }


        Results results = surveysService
                .getSurveyDetails(surveyId,isRespondents);
        return formatterClass.getResponse(results);

    }
    @PutMapping(value = "survey-details/{id}")
    public ResponseEntity<?> updateSurvey(
            @RequestBody DbSurvey dbSurvey,
            @PathVariable("id") String id) {
        Results results = surveysService
                .updateSurvey(id, dbSurvey);
        return formatterClass.getResponse(results);
    }

//    @PostMapping("/add")
//    public ResponseEntity<?> confirmSurvey(
//            @RequestBody DbSurvey dbSurvey){
//        Results results = surveysService
//                .confirmSurvey(dbSurvey);
//        return formatterClass.getResponse(results);
//    }

//    @GetMapping(value = "/admin-respondents/{creatorId}")
//    public ResponseEntity<?> listSurvey(
//            @PathVariable("creatorId") String creatorId,
//            @RequestParam("status") String status
//    ){
//        Results results = surveysService
//                .listRespondentsSurveys(creatorId, status);
//        return formatterClass.getResponse(results);
//
//    }
//    @GetMapping(value = "/{surveyId}")
//    public ResponseEntity<?> surveyDetails(
//            @PathVariable("surveyId") String surveyId){
//        Results results = surveysService
//                .surveyDetails(surveyId);
//        return formatterClass.getResponse(results);
//
//    }
}
