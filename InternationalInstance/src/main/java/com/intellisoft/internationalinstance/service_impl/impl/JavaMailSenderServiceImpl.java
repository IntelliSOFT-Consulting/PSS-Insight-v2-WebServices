package com.intellisoft.internationalinstance.service_impl.impl;

import com.intellisoft.internationalinstance.DbNotificationData;
import com.intellisoft.internationalinstance.FormatterClass;
import com.intellisoft.internationalinstance.db.NotificationSubscription;
import com.intellisoft.internationalinstance.db.repso.NotificationSubscriptionRepo;
import com.intellisoft.internationalinstance.service_impl.service.JavaMailSenderService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JavaMailSenderServiceImpl implements JavaMailSenderService {

    private final FormatterClass formatterClass = new FormatterClass();
    @Value("${API2}")
    private String api2;
    private String api3 = "_3A.WaLPg3rVm6je";
    @Value("${API4}")
    private String api4;
    @Value("${API5}")
    private String api5;
    private String emailAddressAdmin = "pssnotifications";
    private final NotificationSubscriptionRepo notificationSubscriptionRepo;


    public void sendEmailBackground(DbNotificationData dbNotificationData) {

        try{

            String sendGridApi = "S"+api2+api3+api4+api5+"DmxOJa2vBRI";

            Email from = new Email(emailAddressAdmin+"23@gmail.com");
            String subject = "PSS Survey";
            SendGrid sg = new SendGrid(sendGridApi);
            Request request = new Request();

            ClassPathResource sendEmailClassPath = new ClassPathResource("templates/email.html");
            String htmlContent = new String(sendEmailClassPath.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);

            Personalization personalization = new Personalization();
            personalization.addTo(new Email("dnjau@intellisoftkenya.com"));
            personalization.setSubject(subject);

            Mail mail = new Mail();
            mail.setFrom(from);

            mail.setSubject(subject);

//            htmlContent = htmlContent.replace("[EMAIL_ADDRESS]", formatterClass.extractName(emailAddress));
//            htmlContent = htmlContent.replace("[TITLE]", title);
//            htmlContent = htmlContent.replace("[DESCRIPTION]", description);
//
//            mail.addContent(htmlContent);
//            mail.addPersonalization(personalization);
//            try {
//                request.setMethod(Method.POST);
//                request.setEndpoint("mail/send");
//                request.setBody(mail.build());
//                Response response = sg.api(request);
//                System.out.println(response.getStatusCode());
//                System.out.println(response.getBody());
//                System.out.println(response.getHeaders());
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }














//
//            List<String> emailAddressList = dbNotificationData.getEmailAddress();
//            for (String emailAddress: emailAddressList){
//
//                String title = dbNotificationData.getTitle();
//                String description = dbNotificationData.getDescription();
//
//                htmlContent = htmlContent.replace("[EMAIL_ADDRESS]", formatterClass.extractName(emailAddress));
//                htmlContent = htmlContent.replace("[TITLE]", title);
//                htmlContent = htmlContent.replace("[DESCRIPTION]", description);
//
//                Email to = new Email(emailAddress);
//                Content content = new Content("text/html", htmlContent);
//                Mail mail = new Mail(from, subject, to, content);
//                request.setMethod(Method.POST);
//                request.setEndpoint("mail/send");
//                request.setBody(mail.build());
//                Response response = sg.api(request);
//                System.out.println("------");
//                System.out.println(response.getStatusCode());
//                System.out.println(response.getBody());
//            }



        }catch (Exception e){
            System.out.println("========");
            e.printStackTrace();
        }

    }

//    private List<String> getEmailAddress(){
//        List<String> stringList = new ArrayList<>();
//        List<NotificationSubscription> emailAddressList = notificationSubscriptionRepo.findAll();
//        for (NotificationSubscription notificationSubscription: emailAddressList){
//            boolean isActive = notificationSubscription.getIsActive();
//            if (isActive){
//                String emailAddress = notificationSubscription.getEmail();
//                stringList.add(emailAddress);
//            }
//        }
//        return stringList;
//    }
}
