package com.intellisoft.internationalinstance.controller;

import com.intellisoft.internationalinstance.DbTemplateData;
import com.intellisoft.internationalinstance.FormatterClass;
import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.service_impl.InternationalService;
import com.intellisoft.internationalinstance.service_impl.ProgramsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1")
@RestController
@RequiredArgsConstructor
public class DhisController {

    FormatterClass formatterClass = new FormatterClass();

    private final InternationalService internationalService;

    @GetMapping("/indicators")
    public ResponseEntity<?> getIndicatorForFrontEnd() {
        Results results = internationalService.getIndicators();
        return formatterClass.getResponse(results);
    }

}

