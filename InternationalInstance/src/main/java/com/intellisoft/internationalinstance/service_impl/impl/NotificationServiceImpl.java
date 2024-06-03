package com.intellisoft.internationalinstance.service_impl.impl;

import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.db.NotificationEntity;
import com.intellisoft.internationalinstance.db.NotificationSubscription;
import com.intellisoft.internationalinstance.db.VersionEntity;
import com.intellisoft.internationalinstance.db.repso.NotificationEntityRepo;
import com.intellisoft.internationalinstance.db.repso.NotificationSubscriptionRepo;
import com.intellisoft.internationalinstance.model.Response;
import com.intellisoft.internationalinstance.service_impl.service.JavaMailSenderService;
import com.intellisoft.internationalinstance.service_impl.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationSubscriptionRepo notificationSubscriptionRepo;
    private final NotificationEntityRepo notificationEntityRepo;

    private final JavaMailSenderService javaMailSenderService;
    private final FormatterClass formatterClass = new FormatterClass();

    private final EnvConfig envConfig;


    @Override
    public Results subscribe(DbNotificationSub notificationSubscription) {
        try {

            NotificationSubscription subscription = new NotificationSubscription();
            if (notificationSubscription.getId() != null) subscription.setUserId(notificationSubscription.getId());
            if (notificationSubscription.getLastName() != null)
                subscription.setLastName(notificationSubscription.getLastName());
            if (notificationSubscription.getPhoneNumber() != null)
                subscription.setPhone(notificationSubscription.getPhoneNumber());
            if (notificationSubscription.getFirstName() != null)
                subscription.setFirstName(notificationSubscription.getFirstName());
            if (notificationSubscription.getUserId() != null)
                subscription.setUserId(notificationSubscription.getUserId());

            subscription.setEmail(notificationSubscription.getEmail());

            var savedSubscription = notificationSubscriptionRepo.findByEmailAndAndUserId(
                    notificationSubscription.getEmail(),
                    notificationSubscription.getUserId());

            if (savedSubscription.isEmpty()) {
                // user does not exist, so the API can subscribe the user
                notificationSubscriptionRepo.save(subscription);
            } else {
                //their emails are similar, hence avoid double subscription::
                return new Results(400,
                        "A user with the same email and the same organisation already exists.");
            }
            return new Results(200, notificationSubscription);
        } catch (Exception e) {
            log.error("An error occurred while saving subscription for notifications");
            return new Results(400, "Saving resource not possible.");
        }
    }

    @Override
    public Results unsubscribe(String email) {
        var savedSubscription = notificationSubscriptionRepo.findByEmail(email);

        if (savedSubscription.isEmpty()) return new Results(400, "Resource not found.");

        savedSubscription.get().setIsActive(false);
        notificationSubscriptionRepo.save(savedSubscription.get());

        return new Results(200, new DbDetails(savedSubscription.get().getEmail() + " has been unsubscribed successfully."));
    }

    @Override
    public Results getNotificationDetails(String id) {

        Optional<NotificationSubscription> optionalNotificationSubscription = notificationSubscriptionRepo.findById(Long.valueOf(id));
        return optionalNotificationSubscription.map(notificationSubscription -> new Results(200, notificationSubscription)).orElseGet(() -> new Results(400, "Resource not found"));

    }

    @Override
    public Results getNotifications(int no, int size, String emailAddress) {

        Optional<NotificationSubscription> subscriptionOptional = notificationSubscriptionRepo.findByEmail(emailAddress);
        if (subscriptionOptional.isEmpty()) {
            return new Results(400, "We don't have a record of the email address.");
        }
        if (!subscriptionOptional.get().getIsActive()) {
            List<DbNotification> dbNotificationList = new ArrayList<>();
            DbResults dbResults = new DbResults(0, dbNotificationList);
            return new Results(200, dbResults);
        }

        List<DbNotification> dbNotificationList = new ArrayList<>();
        List<NotificationEntity> notificationEntityList = notificationEntityRepo.findByEmailAddress(emailAddress);
        for (NotificationEntity notification : notificationEntityList) {
            DbNotification dbNotification = new DbNotification(notification.getTitle(), notification.getMessage(), notification.getSender(), String.valueOf(notification.getCreatedAt()));
            dbNotificationList.add(dbNotification);
        }
        /**
         * TODO: add pagination for this
         */

//        Pageable pageable = PageRequest.of(no, size);
//        Page<NotificationEntity> page = notificationEntityRepo.findByEmailAddressPage(emailAddress, pageable);
//        List<NotificationEntity> notificationEntities = page.getContent();
        DbResults dbResults = new DbResults(dbNotificationList.size(), dbNotificationList);

        return new Results(200, dbResults);
    }

    @Override
    public Results getSubscribedList(int no, int size) {

        Pageable pageable = PageRequest.of(no, size);
        List<NotificationSubscription> dbNotificationList = notificationSubscriptionRepo.findAllByIsActive(true);
//        List<NotificationSubscription> dbNotificationList = notificationSubscriptionRepo.findAllByIsActive(true,pageable);
        DbResults dbResults = new DbResults(dbNotificationList.size(), dbNotificationList);
        return new Results(200, dbResults);
    }

    public String getHostUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }

    @Override
    public void createNotification(NotificationEntity notificationEntity) {

        try {
            NotificationEntity notification = notificationEntityRepo.save(notificationEntity);
            List<String> emailList = notification.getEmailList();

            if (!emailList.isEmpty()) {
                String baseUrl = envConfig.getValue().getInternationalUrl();

                DbNotificationData dbNotificationData = new DbNotificationData(emailList, String.valueOf(notification.getCreatedAt()), notification.getTitle(), notification.getMessage());
                formatterClass.sendEmailBackground(baseUrl, javaMailSenderService, dbNotificationData);
            }
        } catch (Exception e) {
            log.error("An error occurred while saving notification");
        }

    }

    @Override
    public Results sendNotification(DbSendNotification dbSendNotification) {

        try {

            String message = dbSendNotification.getMessage();
            String title = dbSendNotification.getTitle();
            String sender = dbSendNotification.getSender();

            //There should be a list with the email addresses.
            List<String> emailList = dbSendNotification.getEmailList();
            List<String> dbEmailList = new ArrayList<>();

            boolean sendAll = Boolean.TRUE.equals(dbSendNotification.getSendAll());
            if (!sendAll) {
                // Check if the user wants to send to specific people
                if (emailList.isEmpty()) {
                    // Check if the email list is provided
                    return new Results(400, "If the send all is not selected, the email list cannot be empty");
                } else {
                    dbEmailList = emailList;
                }
            } else {
                List<NotificationSubscription> notificationSubscriptionList = notificationSubscriptionRepo.findAllByIsActive(true);
                for (NotificationSubscription notificationSubscription : notificationSubscriptionList) {
                    String emailAddress = notificationSubscription.getEmail();
                    dbEmailList.add(emailAddress);
                }
            }

            if (!dbEmailList.isEmpty()) {
                //Save to entity
                NotificationEntity notificationEntity = new NotificationEntity();
                notificationEntity.setTitle(title);
                notificationEntity.setMessage(message);
                notificationEntity.setEmailList(dbEmailList);
                notificationEntity.setSender(sender);
                notificationEntityRepo.save(notificationEntity);

                String baseUrl = getHostUrl();

                //Send email address
                DbNotificationData dbNotificationData = new DbNotificationData(dbEmailList, String.valueOf(notificationEntity.getCreatedAt()), notificationEntity.getTitle(), notificationEntity.getMessage());
                formatterClass.sendEmailBackground(baseUrl, javaMailSenderService, dbNotificationData);

                return new Results(200, new DbDetails("Notification has been sent"));
            } else {
                return new Results(400, "We can't find any email list");

            }

        } catch (Exception e) {
            log.error("An error occurred while sending email notification.");
            return new Results(400, "There was an issue sending the email address.");
        }

    }

    @Override
    public Results getSubscriptionDetails(String userId) {

        Optional<NotificationSubscription> optionalNotificationSubscription = notificationSubscriptionRepo.findFirstByUserIdAndIsActiveTrue(userId);
        return optionalNotificationSubscription.map(notificationSubscription -> new Results(200, notificationSubscription)).orElseGet(() -> new Results(400, "Resource not found"));
    }

    @Override
    public Results updateSubscription(DbNotificationSub dbNotificationSub) {

        Optional<NotificationSubscription> optionalNotificationSubscription =
                notificationSubscriptionRepo.findById(
                        Long.valueOf(dbNotificationSub.getId()
                        )
                );

        if (optionalNotificationSubscription.isPresent()) {
            NotificationSubscription notificationSubscription = optionalNotificationSubscription.get();

            // Update the email address
            notificationSubscription.setEmail(dbNotificationSub.getEmail());

            //updating other fields::
            notificationSubscription.setFirstName(dbNotificationSub.getFirstName());
            notificationSubscription.setLastName(dbNotificationSub.getLastName());
            notificationSubscription.setPhone(dbNotificationSub.getPhoneNumber());

            // Save the record
            notificationSubscriptionRepo.save(notificationSubscription);

            return new Results(200, notificationSubscription);
        } else {
            // The record does not exist, so return a 400 error
            return new Results(400, "Resource not found, update not successful");
        }
    }

}
