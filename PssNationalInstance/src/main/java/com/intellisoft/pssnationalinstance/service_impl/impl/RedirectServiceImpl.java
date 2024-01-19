package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.service_impl.service.RedirectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class RedirectServiceImpl implements RedirectService {
    @Autowired
    RestTemplate restTemplate;

    @Override
    public ResponseEntity<String> redirectToGhoPage(String redirectUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        ResponseEntity<String> response = restTemplate.exchange(redirectUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        return ResponseEntity.status(response.getStatusCode()).contentType(MediaType.APPLICATION_JSON).body(response.getBody());
    }
}
