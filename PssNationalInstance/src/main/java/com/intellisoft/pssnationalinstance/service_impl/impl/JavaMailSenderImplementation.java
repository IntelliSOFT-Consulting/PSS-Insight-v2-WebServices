package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.db.MailConfiguration;
import com.intellisoft.pssnationalinstance.service_impl.service.JavaMailSenderService;
import com.intellisoft.pssnationalinstance.service_impl.service.PeriodConfigurationService;
import com.intellisoft.pssnationalinstance.util.MailConfigurationImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
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

            MailConfiguration mailConfiguration = periodConfigurationService.getMailConfiguration();
            if (mailConfiguration != null && mailSender != null) {

                String subject = "PSS Survey";

                String from = mailConfiguration.getFromEmail();

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setSubject(subject);
                helper.setFrom(from);

                Context context = new Context();

                List<DbSurveyRespondentData> dataList = dbRespondents.getRespondents();
                for (DbSurveyRespondentData respondentData : dataList) {

                    String emailAddress = respondentData.getEmailAddress();
                    String expiryDateTime = respondentData.getExpiryDate();
                    String customUrl = respondentData.getCustomUrl();
                    String password = respondentData.getPassword();

                    context.setVariable("GREETING", "Dear " + formatterClass.extractName(emailAddress) + ",");
                    context.setVariable("PASSWORD", password);
                    context.setVariable("ACCESS_LINK", customUrl);
                    context.setVariable("EXPIRY_TIME", formatterClass.getRemainingTime(expiryDateTime));

                    String content = "";
                    if (status.equals(MailStatus.SEND.name())) {
                        content = templateEngine.process("email", context);
                    } else if (status.equals(MailStatus.RESEND.name())) {
                        content = templateEngine.process("resend_email", context);
                    } else if (status.equals(MailStatus.EXPIRED.name())) {
                        content = templateEngine.process("expired_email", context);
                    } else if (status.equals(MailStatus.REMIND.name())) {
                        content = templateEngine.process("email", context);
                    }

                    helper.setTo(emailAddress);
                    helper.setText(content, true);

                    mailSender.send(message);
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while sending email");
        }
    }

}
