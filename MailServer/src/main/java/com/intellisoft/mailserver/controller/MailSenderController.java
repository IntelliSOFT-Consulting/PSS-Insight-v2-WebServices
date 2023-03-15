package com.intellisoft.mailserver.controller;

import com.intellisoft.mailserver.DbRespondents;
import com.intellisoft.mailserver.DbSurveyRespondent;
import com.intellisoft.mailserver.FormatterClass;
import com.intellisoft.mailserver.Results;
import com.intellisoft.mailserver.model.Response;
import com.intellisoft.mailserver.model.SendMailModel;
import com.intellisoft.mailserver.model.TemplateModel;
import com.intellisoft.mailserver.service.JavaMailSender;
import com.intellisoft.mailserver.service.SenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequestMapping(value = "/api/v1/mail-service")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
public class MailSenderController {

    private final JavaMailSender javaMailSender;
    private final SenderService senderService;
    private final FormatterClass formatterClass = new FormatterClass();


    @PostMapping("/send-email")
    public ResponseEntity<?> sendEmail(
            @RequestBody DbRespondents dbRespondents) {
        Results results = javaMailSender.sendMail(dbRespondents);
        return formatterClass.getResponse(results);
    }
    @PostMapping("sendmail")
    public Response sendEmail(SendMailModel sendMailModel)
    {
        return senderService.sendMail(sendMailModel.getEmailsAndNames(), sendMailModel.getSubject(), sendMailModel.getTemplateId(),sendMailModel.getVariables());
    }
}
