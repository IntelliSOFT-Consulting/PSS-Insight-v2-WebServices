package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.service_impl.service.JavaMailSenderService;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class JavaMailSenderImpl implements JavaMailSenderService {

    @Value("${API2}")
    private String api2;
    private String api3 = "_3A.WaLPg3rVm6je";
    @Value("${API4}")
    private String api4;
    @Value("${API5}")
    private String api5;
    private String emailAddressAdmin = "pssnotifications";

    private final FormatterClass formatterClass = new FormatterClass();

    @Async
    public void sendEmailBackground(DbRespondents dbRespondents, String  status) {

        try{

            String sendGridApi = "S"+api2+api3+api4+api5+"DmxOJa2vBRI";

            Email from = new Email(emailAddressAdmin+"23@gmail.com");
            String subject = "PSS Survey";
            SendGrid sg = new SendGrid(sendGridApi);
            Request request = new Request();

            ClassPathResource sendEmailClassPath = new ClassPathResource("templates/email.html");
            ClassPathResource resendEmailClassPath = new ClassPathResource("templates/resend_email.html");
            ClassPathResource expiredEmailClassPath = new ClassPathResource("templates/expired_email.html");
            String htmlContent = "";

            if (status.equals(MailStatus.SEND.name())){
                htmlContent = new String(sendEmailClassPath.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8);
            }else if (status.equals(MailStatus.RESEND.name())){
                htmlContent = new String(resendEmailClassPath.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8);
            }else if (status.equals(MailStatus.EXPIRED.name())){
                htmlContent = new String(expiredEmailClassPath.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8);
            }else if (status.equals(MailStatus.REMIND.name())){
                htmlContent = new String(expiredEmailClassPath.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8);
            }

            List<DbSurveyRespondentData> dataList = dbRespondents.getRespondents();
            for (DbSurveyRespondentData respondentData: dataList){

                String emailAddress = respondentData.getEmailAddress();
                String expiryDateTime = respondentData.getExpiryDate();
                String customUrl = respondentData.getCustomUrl();
                String password = respondentData.getPassword();

                // Replace the placeholders in the email template with the dynamic content
                htmlContent = htmlContent.replace("[EMAIL_ADDRESS]", formatterClass.extractName(emailAddress));
                htmlContent = htmlContent.replace("[PASSWORD]", password);
                htmlContent = htmlContent.replace("[ACCESS_LINK]", customUrl);
                htmlContent = htmlContent.replace("[EXPIRY_TIME]", formatterClass.getRemainingTime(expiryDateTime));

                Email to = new Email(emailAddress);
                Content content = new Content("text/html", htmlContent);
                Mail mail = new Mail(from, subject, to, content);
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);
                System.out.println("------");
                System.out.println(response.getStatusCode());
                System.out.println(response.getBody());
            }

        }catch (Exception e){
            e.printStackTrace();
        }



    }


}
