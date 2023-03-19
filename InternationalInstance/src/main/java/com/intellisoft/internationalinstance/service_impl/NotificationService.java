package com.intellisoft.internationalinstance.service_impl;

import com.intellisoft.internationalinstance.db.NotificationSubscription;
import com.intellisoft.internationalinstance.model.Response;
import com.intellisoft.internationalinstance.model.SendMailModel;

import java.net.URISyntaxException;

public interface NotificationService {
     Response subscribe(NotificationSubscription notificationSubscription);
     Response unsubscribe(String email);
     Response sendMail(SendMailModel sendMailModel) throws URISyntaxException;
}
