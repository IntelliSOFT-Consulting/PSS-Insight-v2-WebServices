package com.intellisoft.internationalinstance.controller;


import com.intellisoft.internationalinstance.DbEmailConfiguration;
import com.intellisoft.internationalinstance.FormatterClass;
import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.service_impl.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/configuration/")
@RestController
@RequiredArgsConstructor
public class ConfigurationController {

    private final ConfigurationService periodConfigurationService;

    private final FormatterClass formatterClass = new FormatterClass();

    @PostMapping("save-mail")
    public ResponseEntity<?> addMailConfiguration(
            @RequestBody DbEmailConfiguration dbEmailConfiguration){
        Results results = periodConfigurationService.saveMailConfiguration(dbEmailConfiguration);
        return formatterClass.getResponse(results);
    }

}
