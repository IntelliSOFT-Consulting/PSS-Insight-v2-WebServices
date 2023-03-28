package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.DbRespondents;
import com.intellisoft.pssnationalinstance.DbSurveyRespondentData;
import com.intellisoft.pssnationalinstance.FormatterClass;
import com.intellisoft.pssnationalinstance.MailStatus;
import com.intellisoft.pssnationalinstance.db.SurveyRespondents;
import com.intellisoft.pssnationalinstance.repository.SurveyRespondentsRepo;
import com.intellisoft.pssnationalinstance.service_impl.service.JavaMailSenderService;
import com.intellisoft.pssnationalinstance.service_impl.service.SurveyRespondentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Log4j2
@RequiredArgsConstructor
public class MyScheduledTask {

    private final FormatterClass formatterClass = new FormatterClass();
    private final SurveyRespondentsService surveyRespondentsService;
    private final JavaMailSenderService javaMailSenderService;
    private final SurveyRespondentsRepo respondentsRepo;

    @Transactional
    @Scheduled(cron = "0 0 1 * * ?")
    public void runTask() {
        // code to run at 01:00 AM every day

        List<DbSurveyRespondentData> respondentDataList = new ArrayList<>();
        List<SurveyRespondents> surveyRespondentsList = surveyRespondentsService.getSurveyRespondents();
        for (SurveyRespondents surveyRespondents : surveyRespondentsList){

            Long id = surveyRespondents.getId();
            String expiryDateTime = surveyRespondents.getExpiryTime();
            boolean isExpiredToday = formatterClass.isExpiredToday(expiryDateTime);
            if (isExpiredToday){
                String emailAddress = surveyRespondents.getEmailAddress();
                String customUrl = surveyRespondents.getCustomUrl();
                String newExpiryDate = formatterClass.getNewDays();
                String otp = formatterClass.getOtp();

                Optional<SurveyRespondents> optionalSurveyRespondents = respondentsRepo.findById(id);
                if (optionalSurveyRespondents.isPresent()){
                    SurveyRespondents dbSurveyRespondents = optionalSurveyRespondents.get();
                    dbSurveyRespondents.setPassword(otp);
                    dbSurveyRespondents.setExpiryTime(newExpiryDate);
                    respondentsRepo.save(dbSurveyRespondents);
                }

                String loginUrl = customUrl + "?id="+id;

                DbSurveyRespondentData dbSurveyRespondentData = new DbSurveyRespondentData(
                        emailAddress,
                        newExpiryDate,
                        loginUrl,
                        otp
                );
                respondentDataList.add(dbSurveyRespondentData);
            }

        }

        DbRespondents dbRespondents = new DbRespondents(respondentDataList);
        javaMailSenderService.sendEmailBackground(dbRespondents, MailStatus.REMIND.name());

    }
}
