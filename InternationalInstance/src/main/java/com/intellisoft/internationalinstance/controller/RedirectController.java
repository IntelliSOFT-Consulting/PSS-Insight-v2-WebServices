package com.intellisoft.internationalinstance.controller;

import com.intellisoft.internationalinstance.util.AppConstants;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Controller
@RequestMapping(value = "/api/v1/redirect")
public class RedirectController {

    private final RestTemplate restTemplate;

    public RedirectController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    @GetMapping("/{indicatorId}")
    public ResponseEntity<String> redirectToExternalPage(
            @PathVariable String indicatorId,
            @RequestParam("$filter") String $filter,
            @RequestParam("$select") String $select
    ) {
        String redirectUrl = AppConstants.PROXY_REDIRECT_URL + indicatorId + "?$filter=" + $filter + "&$select=" + $select;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = restTemplate.exchange(
                redirectUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        return ResponseEntity.status(response.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());
    }

}
