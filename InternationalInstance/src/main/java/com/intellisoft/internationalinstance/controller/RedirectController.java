package com.intellisoft.internationalinstance.controller;

import com.intellisoft.internationalinstance.service_impl.service.RedirectService;
import com.intellisoft.internationalinstance.util.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/api/v1/redirect")
public class RedirectController {
    @Autowired
    RedirectService redirectService;

    @GetMapping("/{indicatorId}")
    public ResponseEntity<String> redirectToGhoPage(@PathVariable String indicatorId, @RequestParam("$filter") String $filter, @RequestParam("$select") String $select) {
        String redirectUrl = AppConstants.PROXY_REDIRECT_URL + indicatorId + "?$filter=" + $filter + "&$select=" + $select;
        return redirectService.redirectToGhoPage(redirectUrl);
    }
}
