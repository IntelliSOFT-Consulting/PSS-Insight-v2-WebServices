package com.intellisoft.internationalinstance.service_impl.impl;

import com.intellisoft.internationalinstance.DbNotificationData;
import com.intellisoft.internationalinstance.DbResults;
import com.intellisoft.internationalinstance.Results;
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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationSubscriptionRepo notificationSubscriptionRepo;
    private final NotificationEntityRepo notificationEntityRepo;

    private final JavaMailSenderService javaMailSenderService;


    @Override
    public Response subscribe(NotificationSubscription notificationSubscription) {
        try{
            var savedSubscription = notificationSubscriptionRepo.findByEmail(notificationSubscription.getEmail());
            if (savedSubscription.isEmpty())
                notificationSubscriptionRepo.save(notificationSubscription);
            else {
                savedSubscription.get().setIsActive(true);
                savedSubscription.get().setPhone(notificationSubscription.getPhone());
                savedSubscription.get().setFirstName(notificationSubscription.getFirstName());
                savedSubscription.get().setLastName(notificationSubscription.getLastName());
                notificationSubscriptionRepo.save(savedSubscription.get());
            }
            return Response.builder()
                    .message("Successfully subscribed")
                    .httpStatus("Success")
                    .httpStatusCode(200)
                    .status("Success")
                    .build();
        }
        catch (Exception e){
            return Response.builder()
                    .message("Unable to subscribe user")
                    .httpStatus("Failed")
                    .httpStatusCode(400)
                    .status("Failed")
                    .build();
        }
    }

    @Override
    public Response unsubscribe(String email) {
        var savedSubscription = notificationSubscriptionRepo.findByEmail(email);
        if (savedSubscription.isEmpty())
            return Response.builder()
                    .message("You have not subscribed")
                    .httpStatus("Success")
                    .httpStatusCode(400)
                    .status("Failed")
                    .build();

        savedSubscription.get().setIsActive(false);
        notificationSubscriptionRepo.save(savedSubscription.get());

        return Response.builder()
                .message("Successfully unsubscribed")
                .httpStatus("Success")
                .httpStatusCode(200)
                .status("Success")
                .build();
    }

    @Override
    public Results getNotifications(int no, int size, String status, String emailAddress) {

        Optional<NotificationSubscription> subscriptionOptional =
                notificationSubscriptionRepo.findByEmail(emailAddress);
        if (subscriptionOptional.isEmpty()){
            return new Results(400, "The user is not subscribed to the notifications.");
        }

        Pageable pageable = PageRequest.of(no, size);
        Page<NotificationEntity> page = notificationEntityRepo.findAll(pageable);
        List<NotificationEntity> notificationEntities = page.getContent();
        DbResults dbResults = new DbResults(
                notificationEntities.size(),
                notificationEntities
        );

        return new Results(200, dbResults);
    }

    @Override
    public Results createNotification(NotificationEntity notificationEntity) {

        try{
            NotificationEntity notification = notificationEntityRepo.save(notificationEntity);

            DbNotificationData dbNotificationData = new DbNotificationData(
                    "",
                    String.valueOf(notification.getCreatedAt()),
                    notification.getTitle(),
                    notification.getMessage()
            );

            javaMailSenderService.sendEmailBackground(dbNotificationData);


            return new Results(200, notification);
        }catch (Exception e){
            return new Results(400, "Failed saving notification.");

        }

    }


}
