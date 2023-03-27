package com.intellisoft.pssnationalinstance.service_impl.impl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MyScheduledTask {

    @Scheduled(cron = "0 0 1 * * ?")
    public void runTask() {
        // code to run at 01:00 AM every day

        /**
         * TODO: Get the expired dates and check if the have expired
         */

    }
}
