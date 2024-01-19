package com.intellisoft.pssnationalinstance.service_impl.service;

import org.springframework.http.ResponseEntity;

public interface RedirectService {
    ResponseEntity<String> redirectToGhoPage(String redirectUrl);
}
