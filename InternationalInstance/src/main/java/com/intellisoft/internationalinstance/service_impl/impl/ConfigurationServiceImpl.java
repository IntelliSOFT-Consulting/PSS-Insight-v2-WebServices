package com.intellisoft.internationalinstance.service_impl.impl;

import com.intellisoft.internationalinstance.DbEmailConfiguration;
import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.db.MailConfiguration;
import com.intellisoft.internationalinstance.db.repso.MailConfigurationRepository;
import com.intellisoft.internationalinstance.service_impl.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class ConfigurationServiceImpl implements ConfigurationService {

    private final MailConfigurationRepository mailConfigurationRepository;

    @Override
    public Results saveMailConfiguration(DbEmailConfiguration dbEmailConfiguration) {

        String serverType = dbEmailConfiguration.getServerType();
        String serverName = dbEmailConfiguration.getServerName();
        String ports = dbEmailConfiguration.getPorts();
        String username = dbEmailConfiguration.getUsername();
        String from = dbEmailConfiguration.getFrom();
        String password = dbEmailConfiguration.getPassword();

        Optional<MailConfiguration> optionalMailConfiguration =
                mailConfigurationRepository.findByServerType(serverType);
        if (optionalMailConfiguration.isPresent()){

            MailConfiguration mailConfiguration = optionalMailConfiguration.get();
            mailConfiguration.setUsername(username);
            mailConfiguration.setPassword(password);
            mailConfiguration.setPorts(ports);
            mailConfiguration.setServerName(serverName);
            mailConfiguration.setFromEmail(from);
            mailConfigurationRepository.save(mailConfiguration);

            return new Results(200, mailConfiguration);
        }else {

            List<MailConfiguration> mailConfigurationList = mailConfigurationRepository.findAll();
            for (MailConfiguration mailConfiguration: mailConfigurationList){
                Optional<MailConfiguration> configurationOptional =
                        mailConfigurationRepository.
                                findByServerType(mailConfiguration.getServerType());
                if (configurationOptional.isPresent()){
                    MailConfiguration newMailConfiguration = configurationOptional.get();
                    newMailConfiguration.setIsActive(false);
                    mailConfigurationRepository.save(newMailConfiguration);
                }

            }

            MailConfiguration mailConfiguration = new MailConfiguration();
            mailConfiguration.setUsername(username);
            mailConfiguration.setPassword(password);
            mailConfiguration.setPorts(ports);
            mailConfiguration.setServerName(serverName);
            mailConfiguration.setServerType(serverType);
            mailConfiguration.setFromEmail(from);
            mailConfiguration.setIsActive(true);
            mailConfigurationRepository.save(mailConfiguration);

            return new Results(200, mailConfiguration);
        }

    }

    @Override
    public MailConfiguration getMailConfiguration() {
        Optional<MailConfiguration> optionalMailConfiguration = mailConfigurationRepository.findByIsActive(true);
        return optionalMailConfiguration.orElseThrow(() -> new NoSuchElementException("No active MailConfiguration found"));
    }
}
