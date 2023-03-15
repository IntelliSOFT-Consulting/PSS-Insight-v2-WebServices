package com.intellisoft.internationalinstance.service_impl;

import com.intellisoft.internationalinstance.db.NotificationSubscription;
import com.intellisoft.internationalinstance.db.repso.NotificationSubscriptionRepo;
import com.intellisoft.internationalinstance.model.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationSubscriptionRepo notificationSubscriptionRepo;
    @Override
    public Response subscribe(NotificationSubscription notificationSubscription) {
        try{
            var savedSubscription = notificationSubscriptionRepo.findByEmail(notificationSubscription.getEmail());
            if (savedSubscription.isEmpty())
                notificationSubscriptionRepo.save(notificationSubscription);
            else
            {
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
                    .message("Unable to subscribe")
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
}
