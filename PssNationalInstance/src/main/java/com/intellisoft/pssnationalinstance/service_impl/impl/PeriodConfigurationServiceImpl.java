package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.db.MailConfiguration;
import com.intellisoft.pssnationalinstance.db.PeriodConfiguration;
import com.intellisoft.pssnationalinstance.db.VersionEntity;
import com.intellisoft.pssnationalinstance.repository.MailConfigurationRepository;
import com.intellisoft.pssnationalinstance.repository.PeriodConfigurationRepo;
import com.intellisoft.pssnationalinstance.service_impl.service.NationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.PeriodConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class PeriodConfigurationServiceImpl implements PeriodConfigurationService {

    private final PeriodConfigurationRepo periodConfigurationRepo;
    private final MailConfigurationRepository mailConfigurationRepository;

    @Override
    public Results addPeriodConfiguration(DbPeriodConfiguration dbPeriodConfiguration) {

        String period = dbPeriodConfiguration.getPeriod();
        String closedBy = dbPeriodConfiguration.getClosedBy();
        boolean isCompleted = dbPeriodConfiguration.isCompleted();

        Optional<PeriodConfiguration> optionalPeriodConfiguration =
                periodConfigurationRepo.findByPeriod(period);
        if (optionalPeriodConfiguration.isPresent()){
            PeriodConfiguration periodConfiguration = optionalPeriodConfiguration.get();
            Long id = periodConfiguration.getId();
            updatePeriodConfiguration(String.valueOf(id), dbPeriodConfiguration);
        }else {
            PeriodConfiguration periodConfiguration = new PeriodConfiguration();
            periodConfiguration.setCompleted(isCompleted);
            periodConfiguration.setCompletedBy(closedBy);
            periodConfiguration.setPeriod(period);
        }

        return new Results(201,
                new DbDetails("Configuration has been captured successfully."));
    }

    @Override
    public Results listPeriodConfiguration(int page, int size) {

        List<PeriodConfiguration> configurationList =
                getPagedConfiguration(page, size, "", "");
        DbResults dbResults = new DbResults(
                configurationList.size(),
                configurationList
        );

        return new Results(200, dbResults);
    }

    private List<PeriodConfiguration> getPagedConfiguration(
            int pageNo,
            int pageSize,
            String sortField,
            String sortDirection) {
        String sortPageField = "";
        String sortPageDirection = "";

        if (sortField.equals("")){sortPageField = "createdAt"; }else {sortPageField = sortField;}
        if (sortDirection.equals("")){sortPageDirection = "DESC"; }else {sortPageDirection = sortField;}

        Sort sort = sortPageDirection.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortPageField).ascending() : Sort.by(sortPageField).descending();
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<PeriodConfiguration> page = periodConfigurationRepo.findAll(pageable);

        return page.getContent();
    }

    @Override
    public Results updatePeriodConfiguration(String id,
                                             DbPeriodConfiguration dbPeriodConfiguration) {

        String period = dbPeriodConfiguration.getPeriod();
        String closedBy = dbPeriodConfiguration.getClosedBy();
        boolean isCompleted = dbPeriodConfiguration.isCompleted();

        Optional<PeriodConfiguration> optionalPeriodConfiguration =
                periodConfigurationRepo.findById(Long.valueOf(id));
        if (optionalPeriodConfiguration.isPresent()) {
            PeriodConfiguration periodConfiguration = optionalPeriodConfiguration.get();
            periodConfiguration.setCompleted(isCompleted);
            periodConfiguration.setCompletedBy(closedBy);
            periodConfiguration.setPeriod(period);
            periodConfigurationRepo.save(periodConfiguration);
            return new Results(200,
                    new DbDetails("Configuration has been updated"));
        }

        return new Results(400, "Resource not found");
    }

    @Override
    public PeriodConfiguration getConfigurationDetails(String period) {

        Optional<PeriodConfiguration> optionalPeriodConfiguration =
                periodConfigurationRepo.findByPeriod(period);
        return optionalPeriodConfiguration.orElse(null);
    }

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

        Optional<MailConfiguration> optionalMailConfiguration =
                mailConfigurationRepository.findByIsActive(true);
        return optionalMailConfiguration.orElse(null);

    }
}
