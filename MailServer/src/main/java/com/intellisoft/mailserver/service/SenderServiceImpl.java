package com.intellisoft.mailserver.service;

import com.intellisoft.mailserver.db.respos.MailTemplateRepo;
import com.intellisoft.mailserver.model.Response;
import com.sendgrid.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Log4j2
public class SenderServiceImpl implements SenderService {
    private final MailTemplateRepo mailTemplateRepo;
    @Value("${API2}")
    private String api2;
    private String api3 = "_3A.WaLPg3rVm6je";
    @Value("${API4}")
    private String api4;
    @Value("${API5}")
    private String api5;

    @Override
    public Response sendMail(List<Map<String,String>> emailsAndNames, String subject, Long templateId, Map<String, Object> variables) {
        String sendGridApi = "S"+api2+api3+api4+api5+"DmxOJa2vBRI";
        String emailAddressAdmin = "pssnotifications";
        Email from = new Email(emailAddressAdmin +"23@gmail.com");

        //message variables must be in the format {variableName}
        //and then in the map we do variableName = value
        var mailTemplate = mailTemplateRepo.findById(templateId).orElse(null);
        if (mailTemplate == null) {
            return new Response("false", "Mail template not found");
        }
        String message = mailTemplate.getTemplateContent();

        SendGrid sg = new SendGrid(sendGridApi);
        Request request = new Request();
        for (Map<String, String > toEmailAndName : emailsAndNames) {
            Email email = new Email();
            for (Map.Entry<String, String> entry : toEmailAndName.entrySet()) {
                String emailAdd = entry.getKey();
                String names = entry.getValue();
                email = new Email(emailAdd, names);
            }
            String messageContent = constructMessage(message, variables);
            Content content = new Content("text/html", messageContent);
            Mail mail = new Mail(from, subject, email, content);
            try {
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                // TODO: 15/03/2023  use executorService to execute the request
                com.sendgrid.Response response = sg.api(request);


                log.info("Response: " + response.getStatusCode() + " " + response.getBody());
                return Response.builder().message(response.getBody()).status(response.getStatusCode()+"").build();


            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return Response.builder().message("Message has been sent").status("200").build();
    }
    private String constructMessage(String message, Map<String, Object> variables) {
        if (variables != null) {
            variables.forEach((key, value) -> {
                message.replace("{"+key+"}", String.valueOf(value));
            });
        }
        return message;

    }
}
