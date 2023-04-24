package com.intellisoft.pssnationalinstance.util;

import com.intellisoft.pssnationalinstance.db.MailConfiguration;
import com.intellisoft.pssnationalinstance.service_impl.service.PeriodConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class MailConfigurationImpl {

    private final PeriodConfigurationService periodConfigurationService;

    @Bean
    public JavaMailSender javaMailSender() {

        MailConfiguration mailConfiguration =
                periodConfigurationService.getMailConfiguration();
        if (mailConfiguration != null){

            String serverName = mailConfiguration.getServerName();
            String ports = mailConfiguration.getPorts();
            String username = mailConfiguration.getUsername();
            String serverPassword = mailConfiguration.getPassword();

            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(serverName);
            mailSender.setPort(Integer.parseInt(ports));

            mailSender.setUsername(username);
            mailSender.setPassword(serverPassword);

            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.debug", "true");

            return mailSender;
        }

        return null;

    }

}
