package com.intellisoft.pssnationalinstance.controller;

import com.intellisoft.pssnationalinstance.DbNotificationSub;
import com.intellisoft.pssnationalinstance.FormatterClass;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.service_impl.service.JavaMailSenderService;
import com.intellisoft.pssnationalinstance.service_impl.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> unsubscribe(@RequestBody DbNotificationSub notificationSubscription)  {

        Results results = notificationService.unsubscribe(notificationSubscription);
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

    @GetMapping("/list-national-subscribers")
    public ResponseEntity<?> getNationalSubscribers(
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
        Results results = notificationService.getNationalSubscribers(limitNo, pageNumber);
        return formatterClass.getResponse(results);
    }

    @GetMapping("/subscription-details")
    public ResponseEntity<?> getSubscriptionDetails(@RequestParam("userId") String userId) {
        Results results = notificationService.getSubscriptionDetails(userId);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @PutMapping("/update-subscription")
    public ResponseEntity<Results> updateSubscription(@RequestBody DbNotificationSub dbNotificationSub) {
        return ResponseEntity.status(HttpStatus.OK).body(notificationService.updateSubscription(dbNotificationSub));
    }

}
