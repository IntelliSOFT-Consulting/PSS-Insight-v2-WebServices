package com.intellisoft.internationalinstance.service_impl.service;

import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.db.NotificationEntity;
import com.intellisoft.internationalinstance.db.NotificationSubscription;
import com.intellisoft.internationalinstance.model.Response;

public interface NotificationService {
     Response subscribe(NotificationSubscription notificationSubscription);
     Response unsubscribe(String email);

     Results getNotifications(int no, int size, String status, String emailAddress);

     Results createNotification(NotificationEntity notificationEntity);
}
