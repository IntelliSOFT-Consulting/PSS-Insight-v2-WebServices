package com.intellisoft.internationalinstance.service_impl;

import com.intellisoft.internationalinstance.db.NotificationSubscription;
import com.intellisoft.internationalinstance.model.Response;

public interface NotificationService {
     Response subscribe(NotificationSubscription notificationSubscription);
     Response unsubscribe(String email);
}
