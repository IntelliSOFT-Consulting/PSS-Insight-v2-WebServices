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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationSubscriptionRepo notificationSubscriptionRepo;
    private final NotificationEntityRepo notificationEntityRepo;

    private final JavaMailSenderService javaMailSenderService;


    @Override
    public Results subscribe(DbNotificationSub notificationSubscription) {
        try{

            NotificationSubscription subscription = new NotificationSubscription();
            if (notificationSubscription.getLastName() != null) subscription.setLastName(notificationSubscription.getLastName());
            if (notificationSubscription.getPhoneNumber() != null) subscription.setPhone(notificationSubscription.getPhoneNumber());
            if (notificationSubscription.getFirstName() != null) subscription.setFirstName(notificationSubscription.getFirstName());

            subscription.setEmail(notificationSubscription.getEmail());


            var savedSubscription = notificationSubscriptionRepo.findByEmail(notificationSubscription.getEmail());
            if (savedSubscription.isEmpty()){
                notificationSubscriptionRepo.save(subscription);
            } else {
                savedSubscription.get().setIsActive(true);

                if (notificationSubscription.getLastName() != null) savedSubscription.get().setLastName(notificationSubscription.getLastName());
                if (notificationSubscription.getPhoneNumber() != null) savedSubscription.get().setPhone(notificationSubscription.getPhoneNumber());
                savedSubscription.get().setPhone(notificationSubscription.getPhoneNumber());
                savedSubscription.get().setFirstName(notificationSubscription.getFirstName());

                notificationSubscriptionRepo.save(savedSubscription.get());
            }

            return new Results(200, notificationSubscription);
        }
        catch (Exception e){
            e.printStackTrace();
            return new Results(400, "Saving resource not possible.");
        }
    }

    @Override
    public Results unsubscribe(String email) {
        var savedSubscription = notificationSubscriptionRepo.findByEmail(email);
        if (savedSubscription.isEmpty())
            return new Results(400, "Resource not found.");

        savedSubscription.get().setIsActive(false);
        notificationSubscriptionRepo.save(savedSubscription.get());

        return new Results(200,
                new DbDetails(savedSubscription.get().getEmail() + " has been unsubscribed successfully."));
    }

    @Override
    public Results getNotifications(int no, int size,String emailAddress) {

        Optional<NotificationSubscription> subscriptionOptional =
                notificationSubscriptionRepo.findByEmail(emailAddress);
        if (subscriptionOptional.isEmpty()){
            return new Results(400, "We don't have a record of the email address.");
        }
        if (!subscriptionOptional.get().getIsActive()){
            return new Results(400, "The user is not subscribed to the notifications.");
        }

        List<DbNotification> dbNotificationList = new ArrayList<>();
        List<NotificationEntity> notificationEntityList = notificationEntityRepo.findByEmailAddress(emailAddress);
        for (NotificationEntity notification: notificationEntityList){
            DbNotification dbNotification = new DbNotification(
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.getSender(),
                    String.valueOf(notification.getCreatedAt())
            );
            dbNotificationList.add(dbNotification);
        }
        /**
         * TODO: add pagination for this
         */

//        Pageable pageable = PageRequest.of(no, size);
//        Page<NotificationEntity> page = notificationEntityRepo.findByEmailAddressPage(emailAddress, pageable);
//        List<NotificationEntity> notificationEntities = page.getContent();
        DbResults dbResults = new DbResults(
                dbNotificationList.size(),
                dbNotificationList
        );

        return new Results(200, dbResults);
    }

    @Override
    public Results createNotification(NotificationEntity notificationEntity) {
        System.out.println("************ 3");
        try{
            NotificationEntity notification = notificationEntityRepo.save(notificationEntity);
            List<String> emailList = notification.getEmailList();

            if (!emailList.isEmpty()){
                DbNotificationData dbNotificationData = new DbNotificationData(
                        emailList,
                        String.valueOf(notification.getCreatedAt()),
                        notification.getTitle(),
                        notification.getMessage()
                );
                javaMailSenderService.sendEmailBackground(dbNotificationData);
                return new Results(200, notification);
            }else {
                return new Results(400, "Email list is empty.");

            }

        }catch (Exception e){
            System.out.println("************ 3 error");
            return new Results(400, "Failed saving notification.");

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
            if (!sendAll){
                // Check if the user wants to send to specific people
                if(!emailList.isEmpty()){
                    // Check if the email list is provided
                    return new Results(400, "If the send all is not selected, the email list cannot be empty");
                }else {
                    dbEmailList = emailList;
                }
            }else {
                List<NotificationSubscription> notificationSubscriptionList =
                        notificationSubscriptionRepo.findAllByIsActive(true);
                for (NotificationSubscription notificationSubscription: notificationSubscriptionList){
                    String emailAddress = notificationSubscription.getEmail();
                    dbEmailList.add(emailAddress);
                }
            }

            if (!dbEmailList.isEmpty()){
                //Save to entity
                NotificationEntity notificationEntity = new NotificationEntity();
                notificationEntity.setTitle(title);
                notificationEntity.setMessage(message);
                notificationEntity.setEmailList(dbEmailList);
                notificationEntity.setSender(sender);
                notificationEntityRepo.save(notificationEntity);

                //Send email address
                DbNotificationData dbNotificationData = new DbNotificationData(
                        dbEmailList,
                        String.valueOf(notificationEntity.getCreatedAt()),
                        notificationEntity.getTitle(),
                        notificationEntity.getMessage()
                );
                javaMailSenderService.sendEmailBackground(dbNotificationData);

                return new Results(200, new DbDetails("Notification has been sent"));
            }else {
                return new Results(400, "We can't find any email list");

            }

        }catch (Exception e){
            e.printStackTrace();
            return new Results(400, "There was an issue sending the email address.");
        }

    }



}
