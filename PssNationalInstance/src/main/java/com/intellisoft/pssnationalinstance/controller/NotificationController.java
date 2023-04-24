package com.intellisoft.pssnationalinstance.controller;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.service_impl.impl.SurveyRespondentsServiceImpl;
import com.intellisoft.pssnationalinstance.service_impl.service.JavaMailSenderService;
import com.intellisoft.pssnationalinstance.service_impl.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/notification/")
@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final JavaMailSenderService javaMailSenderService;
    FormatterClass formatterClass = new FormatterClass();

    @PostMapping("subscribe")
    public ResponseEntity<?> subscribe(@RequestBody DbNotificationSub notificationSubscription)  {

        Results results = notificationService.subscribe(notificationSubscription);
        return formatterClass.getResponse(results);
    }
    @PutMapping("unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestParam("email") String email)  {

        Results results = notificationService.unsubscribe(email);
        return formatterClass.getResponse(results);

    }

    @GetMapping("/list")
    public ResponseEntity<?> getIndicatorForFrontEnd(
            @RequestParam("email") String email,
            @RequestParam(value = "limit", required = false) String limit,
            @RequestParam(value = "pageNo", required = false) String pageNo
    ) {

        int limitNo = 10;
        if (limit != null && !limit.equals("")){
            limitNo = Integer.parseInt(limit);
        }
        int pageNumber = 1;
        if (pageNo != null && !pageNo.equals("")){
            pageNumber = Integer.parseInt(pageNo);
        }
        Results results = notificationService.getNotifications(limitNo, pageNumber, email);
        return formatterClass.getResponse(results);
    }




}
