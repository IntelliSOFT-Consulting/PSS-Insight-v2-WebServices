package com.intellisoft.internationalinstance.controller;

import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.db.NotificationSubscription;
import com.intellisoft.internationalinstance.model.Response;
import com.intellisoft.internationalinstance.service_impl.service.InternationalService;
import com.intellisoft.internationalinstance.service_impl.service.NotificationService;
import com.intellisoft.internationalinstance.service_impl.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping(value = "/api/v1/notification/")
@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
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

    @PostMapping("send")
    public ResponseEntity<?> sendNotification(@RequestBody DbSendNotification dbSendNotification)  {

        Results results = notificationService.sendNotification(dbSendNotification);
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
