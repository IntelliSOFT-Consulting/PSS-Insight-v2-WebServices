package com.intellisoft.pssnationalinstance.service_impl.service;


import com.intellisoft.pssnationalinstance.DbNotificationSub;
import com.intellisoft.pssnationalinstance.Results;

public interface NotificationService {
     Results subscribe(DbNotificationSub notificationSubscription);
     Results unsubscribe(String email);

     Results getNotifications(int no, int size, String emailAddress);


     Results getSubscriptionDetails(String userId);

     Results updateSubscription(DbNotificationSub dbNotificationSub);
}
