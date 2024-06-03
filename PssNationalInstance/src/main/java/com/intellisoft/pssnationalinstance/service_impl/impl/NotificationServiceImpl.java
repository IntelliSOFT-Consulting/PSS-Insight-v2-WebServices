package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.db.NotificationDbSubscription;
import com.intellisoft.pssnationalinstance.repository.NotificationDbSubscriptionRepo;
import com.intellisoft.pssnationalinstance.service_impl.service.NotificationService;
import com.intellisoft.pssnationalinstance.util.EnvUrlConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    WebClient webClient;

    private final EnvUrlConstants envUrlConstants;

    private final NotificationDbSubscriptionRepo notificationDbSubscriptionRepo;

    @Override
    public Results subscribe(DbNotificationSub notificationSubscription) {

        /**
         * Subscribe to the international instance
         * Save the subscriber into the local instance
         *
         * 1. Check if user exists in the local instance
         * 2. Save user in local instance
         * 3. Push data to international instance
         */
        String email = notificationSubscription.getEmail();
        Optional<NotificationDbSubscription> isEmailExists = notificationDbSubscriptionRepo.findByEmail(email);
        if (isEmailExists.isPresent()){
            return new Results(400, "Email already exists. Cannot be added.");
        }

        NotificationDbSubscription subscription = new NotificationDbSubscription();
//        if (notificationSubscription.getId() != null) subscription.setUserId(notificationSubscription.getId());
        if (notificationSubscription.getLastName() != null)
            subscription.setLastName(notificationSubscription.getLastName());
        if (notificationSubscription.getPhoneNumber() != null)
            subscription.setPhone(notificationSubscription.getPhoneNumber());
        if (notificationSubscription.getFirstName() != null)
            subscription.setFirstName(notificationSubscription.getFirstName());
        if (notificationSubscription.getOrganisationId() != null)
            subscription.setOrganisationId(notificationSubscription.getOrganisationId());
        if (notificationSubscription.getUserId() != null)
            subscription.setUserId(notificationSubscription.getUserId());

        subscription.setEmail(notificationSubscription.getEmail());
        notificationDbSubscriptionRepo.save(subscription);

        String internationalBaseApi = envUrlConstants.getINTERNATIONAL_NOTIFICATION() + "subscribe";
        return getPostResults(notificationSubscription, internationalBaseApi);

    }

    private Results getPostResults(DbNotificationSub notificationSubscription, String internationalBaseApi) {

        try {
            DbResultsApi dbResultsApi = GenericWebclient.postForSingleObjResponse(internationalBaseApi, notificationSubscription, DbNotificationSub.class, DbResultsApi.class);
            if (dbResultsApi == null) {
                return new Results(400, "There was an issue processing the request");
            }

            if (dbResultsApi.getCode() == 200) {
                return new Results(200, dbResultsApi.getDetails());
            } else {
                return new Results(400, dbResultsApi.getDetails());
            }
        } catch (Exception e) {
            log.error("An error occurred while processing POST request to the global instance");
            return new Results(400, "There was an issue processing the request");

        }


    }

    @Override
    public Results unsubscribe(DbNotificationSub notificationSubscription) {

        /**
         * Update this deactivation too on the local instance too
         */
        String email = notificationSubscription.getEmail();
        Optional<NotificationDbSubscription> optionalNotificationDbSubscription = notificationDbSubscriptionRepo.findByEmail(email);
        if (optionalNotificationDbSubscription.isEmpty()){
            return new Results(400, "Email does not already exist.");
        }
        NotificationDbSubscription savedSubscription = optionalNotificationDbSubscription.get();

        String internationalBaseApi = envUrlConstants.getINTERNATIONAL_NOTIFICATION() + "unsubscribe-email";
        Results results = getPostResults(notificationSubscription, internationalBaseApi);
//        int code = results.getCode();
//        if (code != 200)
//            return results;

        savedSubscription.setIsActive(false);
        NotificationDbSubscription saved = notificationDbSubscriptionRepo.save(savedSubscription);

        return results;
    }

    @NotNull
    private Results getIntResults(String internationalBaseApi) {
        try {

            DbResultsApi dbResultsApi = GenericWebclient.getForSingleObjResponse(internationalBaseApi, DbResultsApi.class);
            if (dbResultsApi == null) return new Results(400, "There was an issue processing the request");

            if (dbResultsApi.getCode() == 200) {
                return new Results(200, dbResultsApi.getDetails());
            } else {
                return new Results(400, dbResultsApi.getDetails());
            }

        } catch (Exception e) {
            log.error("An error occurred when fetching document details");
            return new Results(400, "There was an issue processing the request");

        }
    }

    @Override
    public Results getNotifications(int no, int size, String emailAddress) {
        String internationalBaseApi = envUrlConstants.getINTERNATIONAL_NOTIFICATION() + "list?email=" + emailAddress;
        return getIntResults(internationalBaseApi);
    }

    @Override
    public Results getNationalSubscribers(int no, int size) {

        List<NotificationDbSubscription> notificationList = notificationDbSubscriptionRepo.findAll();
        return new Results(200, new DbResults(notificationList.size(), notificationList));
    }

    public Results getSubscriptionDetails(String userId) {
        String apiUrl = envUrlConstants.getINTERNATIONAL_NOTIFICATION() + "subscription-details";

        try {
            NotificationSubscription notificationSubscription = webClient.get().uri(apiUrl + "?userId={userId}", userId).retrieve().bodyToMono(NotificationSubscription.class).block();

            if (notificationSubscription == null) {
                return new Results(400, "There was an issue processing the request");
            }
            return new Results(200, notificationSubscription);
        } catch (Exception e) {
            log.error("An error occurred while fetching subscription details");
            return new Results(400, "An error occurred while processing the request, subscription details not found or is inactive");
        }
    }

    public Results updateSubscription(DbNotificationSub dbNotificationSub) {
        String internationalBaseApi = envUrlConstants.getINTERNATIONAL_NOTIFICATION() + "update-subscription";

        try {
            WebClient webClient = WebClient.create();

            // Send the update request with the provided DbNotificationSub object
            UnsubscribeResponse response = webClient.put().uri(internationalBaseApi).bodyValue(dbNotificationSub).retrieve().bodyToMono(UnsubscribeResponse.class).block();

            if (response != null && response.getCode() == 200 && response.getDetails() != null) {

                NotificationSubscription details = response.getDetails();
                NotificationSubscription notificationSubscription = new NotificationSubscription(
                        details.getId(),
                        details.getFirstName(),
                        details.getLastName(),
                        details.getEmail(),
                        details.getPhone(),
                        details.isActive(),
                        details.getUserId());

                //Update the user info
                Long id = notificationSubscription.getId();
                assert id != null;
                Optional<NotificationDbSubscription> optionalNotificationSubscription = notificationDbSubscriptionRepo.findById(id);
                if (optionalNotificationSubscription.isPresent()){
                    NotificationDbSubscription savedSubscription = getSavedSubscription(dbNotificationSub, optionalNotificationSubscription, notificationSubscription);
                    notificationDbSubscriptionRepo.save(savedSubscription);
                }

                return new Results(200, notificationSubscription);
            } else {
                return new Results(400, "Resource not found, update not successful");
            }
        } catch (Exception e) {
            log.error("An error occurred while updating subscription details");
            return new Results(400, "An error occurred while processing the request");
        }
    }

    @NotNull
    private static NotificationDbSubscription getSavedSubscription(DbNotificationSub dbNotificationSub, Optional<NotificationDbSubscription> optionalNotificationSubscription, NotificationSubscription notificationSubscription) {
        NotificationDbSubscription savedSubscription = optionalNotificationSubscription.get();

        if (notificationSubscription.getId() != null) savedSubscription.setUserId(dbNotificationSub.getId());
        if (notificationSubscription.getLastName() != null)
            savedSubscription.setLastName(dbNotificationSub.getLastName());
        if (notificationSubscription.getPhone() != null)
            savedSubscription.setPhone(dbNotificationSub.getPhoneNumber());
        if (notificationSubscription.getFirstName() != null)
            savedSubscription.setFirstName(dbNotificationSub.getFirstName());
        if (dbNotificationSub.getOrganisationId() != null)
            savedSubscription.setOrganisationId(dbNotificationSub.getOrganisationId());
        return savedSubscription;
    }


}