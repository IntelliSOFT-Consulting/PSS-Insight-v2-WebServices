package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.db.MailConfiguration;
import com.intellisoft.pssnationalinstance.service_impl.service.JavaMailSenderService;
import com.intellisoft.pssnationalinstance.service_impl.service.PeriodConfigurationService;
import com.intellisoft.pssnationalinstance.util.MailConfigurationImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class JavaMailSenderImplementation implements JavaMailSenderService {

    private final PeriodConfigurationService periodConfigurationService;
    private final FormatterClass formatterClass = new FormatterClass();
    private final TemplateEngine templateEngine;

    private final MailConfigurationImpl mailConfigurationImpl;

    @Override
    public void sendEmailBackground(DbRespondents dbRespondents, String status) {

        try {

            JavaMailSender mailSender = mailConfigurationImpl.javaMailSender();

            MailConfiguration mailConfiguration =
                    periodConfigurationService.getMailConfiguration();
            if (mailConfiguration != null && mailSender != null){

                String subject = "PSS Survey";

                String from = mailConfiguration.getFromEmail();

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setSubject(subject);
                helper.setFrom(from);

                Context context = new Context();

                List<DbSurveyRespondentData> dataList = dbRespondents.getRespondents();
                for (DbSurveyRespondentData respondentData: dataList){

                    String emailAddress = respondentData.getEmailAddress();
                    String expiryDateTime = respondentData.getExpiryDate();
                    String customUrl = respondentData.getCustomUrl();
                    String password = respondentData.getPassword();

                    context.setVariable("GREETING", "Dear "+formatterClass.extractName(emailAddress)+",");
                    context.setVariable("PASSWORD", password);
                    context.setVariable("ACCESS_LINK", customUrl);
                    context.setVariable("EXPIRY_TIME", formatterClass.getRemainingTime(expiryDateTime));

                    String content = "";
                    if (status.equals(MailStatus.SEND.name())){
                        content = templateEngine.process("email", context);
                    }else if (status.equals(MailStatus.RESEND.name())){
                        content = templateEngine.process("resend_email", context);
                    }else if (status.equals(MailStatus.EXPIRED.name())){
                        content = templateEngine.process("expired_email", context);
                    }else if (status.equals(MailStatus.REMIND.name())){
                        content = templateEngine.process("email", context);
                    }

                    helper.setTo(emailAddress);
                    helper.setText(content, true);

                    mailSender.send(message);


                }










            }

        }catch (Exception e){
            e.printStackTrace();
        }




    }

//    @Value("${API2}")
//    private String api2;
//    private String api3 = "_3A.WaLPg3rVm6je";
//    @Value("${API4}")
//    private String api4;
//    @Value("${API5}")
//    private String api5;
//    private String emailAddressAdmin = "pssnotifications";
//
//
//    @Async
//    public void sendEmailBackground(DbRespondents dbRespondents, String  status) {
//
//        try{
//
//            String sendGridApi = "S"+api2+api3+api4+api5+"DmxOJa2vBRI";
//
//            Email from = new Email(emailAddressAdmin+"23@gmail.com");
//            String subject = "PSS Survey";
//            SendGrid sg = new SendGrid(sendGridApi);
//            Request request = new Request();
//
//            ClassPathResource sendEmailClassPath = new ClassPathResource("templates/email.html");
//            ClassPathResource resendEmailClassPath = new ClassPathResource("templates/resend_email.html");
//            ClassPathResource expiredEmailClassPath = new ClassPathResource("templates/expired_email.html");
//            String htmlContent = "";
//
//            if (status.equals(MailStatus.SEND.name())){
//                htmlContent = new String(sendEmailClassPath.getInputStream().readAllBytes(),
//                        StandardCharsets.UTF_8);
//            }else if (status.equals(MailStatus.RESEND.name())){
//                htmlContent = new String(resendEmailClassPath.getInputStream().readAllBytes(),
//                        StandardCharsets.UTF_8);
//            }else if (status.equals(MailStatus.EXPIRED.name())){
//                htmlContent = new String(expiredEmailClassPath.getInputStream().readAllBytes(),
//                        StandardCharsets.UTF_8);
//            }else if (status.equals(MailStatus.REMIND.name())){
//                htmlContent = new String(expiredEmailClassPath.getInputStream().readAllBytes(),
//                        StandardCharsets.UTF_8);
//            }
//
//            List<DbSurveyRespondentData> dataList = dbRespondents.getRespondents();
//            for (DbSurveyRespondentData respondentData: dataList){
//
//                String emailAddress = respondentData.getEmailAddress();
//                String expiryDateTime = respondentData.getExpiryDate();
//                String customUrl = respondentData.getCustomUrl();
//                String password = respondentData.getPassword();
//
//                // Replace the placeholders in the email template with the dynamic content
//                htmlContent = htmlContent.replace("[EMAIL_ADDRESS]", formatterClass.extractName(emailAddress));
//                htmlContent = htmlContent.replace("[PASSWORD]", password);
//                htmlContent = htmlContent.replace("[ACCESS_LINK]", customUrl);
//                htmlContent = htmlContent.replace("[EXPIRY_TIME]", formatterClass.getRemainingTime(expiryDateTime));
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
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//
//
//    }


}
