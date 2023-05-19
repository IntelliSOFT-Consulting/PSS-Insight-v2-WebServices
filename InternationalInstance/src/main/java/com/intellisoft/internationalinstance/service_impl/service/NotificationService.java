package com.intellisoft.internationalinstance.service_impl.service;

import com.intellisoft.internationalinstance.DbNotificationSub;
import com.intellisoft.internationalinstance.DbSendNotification;
import com.intellisoft.internationalinstance.Results;
import com.intellisoft.internationalinstance.db.NotificationEntity;
import com.intellisoft.internationalinstance.db.NotificationSubscription;
import com.intellisoft.internationalinstance.model.Response;

public interface NotificationService {
     Results subscribe(DbNotificationSub notificationSubscription);
     Results unsubscribe(String email);
     Results getNotificationDetails(String id);

     Results getNotifications(int no, int size, String emailAddress);
     Results getSubscribedList(int no, int size);

     void createNotification(NotificationEntity notificationEntity);

     Results sendNotification(DbSendNotification dbSendNotification);

     Results getSubscriptionDetails(String userId);

     Results updateSubscription(DbNotificationSub dbNotificationSub);
}
