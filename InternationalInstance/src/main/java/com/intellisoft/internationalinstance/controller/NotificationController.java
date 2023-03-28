package com.intellisoft.internationalinstance.controller;

import com.intellisoft.internationalinstance.DbVersionData;
import com.intellisoft.internationalinstance.FormatterClass;
import com.intellisoft.internationalinstance.Results;
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
    public Response subscribe(@RequestBody NotificationSubscription notificationSubscription)  {
        return notificationService.subscribe(notificationSubscription);
    }
    @PutMapping("unsubscribe")
    public Response unsubscribe(@RequestParam("email") String email)  {
        return notificationService.unsubscribe(email);
    }




}
