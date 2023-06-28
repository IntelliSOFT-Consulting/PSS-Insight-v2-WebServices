package com.intellisoft.internationalinstance.service_impl.service;

import org.springframework.http.ResponseEntity;

public interface RedirectService {
    ResponseEntity<String> redirectToGhoPage(String redirectUrl);
}
