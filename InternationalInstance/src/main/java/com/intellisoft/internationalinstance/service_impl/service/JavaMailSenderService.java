package com.intellisoft.internationalinstance.service_impl.service;

import com.intellisoft.internationalinstance.DbNotificationData;


public interface JavaMailSenderService {

    void sendEmailBackground(DbNotificationData dbNotificationData);
}
