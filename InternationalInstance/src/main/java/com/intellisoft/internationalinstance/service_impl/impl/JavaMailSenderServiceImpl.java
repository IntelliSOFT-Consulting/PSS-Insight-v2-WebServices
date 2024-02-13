package com.intellisoft.internationalinstance.service_impl.impl;

import com.intellisoft.internationalinstance.DbNotificationData;
import com.intellisoft.internationalinstance.FormatterClass;
import com.intellisoft.internationalinstance.db.MailConfiguration;
import com.intellisoft.internationalinstance.db.NotificationSubscription;
import com.intellisoft.internationalinstance.db.repso.NotificationSubscriptionRepo;
import com.intellisoft.internationalinstance.service_impl.service.ConfigurationService;
import com.intellisoft.internationalinstance.service_impl.service.JavaMailSenderService;
import com.intellisoft.internationalinstance.util.MailConfigurationImpl;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class JavaMailSenderServiceImpl implements JavaMailSenderService {

    private final FormatterClass formatterClass = new FormatterClass();

    private final TemplateEngine templateEngine;

    private final MailConfigurationImpl mailConfigurationImpl;
    private final ConfigurationService periodConfigurationService;
    @Value("${server.port}")
    private String serverPort;

    public void sendEmailBackground(String baseUrl, DbNotificationData dbNotificationData) {

        try{

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

                List<String> emailAddressList = dbNotificationData.getEmailAddress();
                for (String emailAddress: emailAddressList) {
                    //Add a unsubscribe link
                    String url = baseUrl+"/api/v1/notification/unsubscribe?email="+emailAddress;
                    context.setVariable("unsubscribe_link", url);

                    String title = dbNotificationData.getTitle();
                    String description = dbNotificationData.getDescription();

                    context.setVariable("GREETING", "Dear "+formatterClass.extractName(emailAddress)+",");
                    context.setVariable("TITLE", title);
                    context.setVariable("DESCRIPTION", description);

                    String content = templateEngine.process("email", context);
                    helper.setTo(emailAddress);
                    helper.setText(content, true);

                    mailSender.send(message);

                }
            }





        }catch (Exception e){
            e.printStackTrace();
            log.error("An error occurred while processing the send email background task");
        }

    }
}
