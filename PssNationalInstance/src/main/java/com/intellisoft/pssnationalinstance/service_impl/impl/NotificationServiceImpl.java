package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.service_impl.service.NotificationService;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    WebClient webClient;

    @Override
    public Results subscribe(DbNotificationSub notificationSubscription) {

        String internationalBaseApi = AppConstants.INTERNATIONAL_NOTIFICATION +"subscribe";
        return getPostResults(notificationSubscription, internationalBaseApi);

    }

    private Results getPostResults(DbNotificationSub notificationSubscription, String internationalBaseApi)  {

        try{
            DbResultsApi dbResultsApi = GenericWebclient.postForSingleObjResponse(
                    internationalBaseApi,
                    notificationSubscription,
                    DbNotificationSub.class,
                    DbResultsApi.class);
            if (dbResultsApi == null) {
                return new Results(400, "There was an issue processing the request");
            }

            System.out.println("**** " + dbResultsApi);

            if (dbResultsApi.getCode() == 200) {
                return new Results(200, dbResultsApi.getDetails());
            }else {
                return new Results(400, dbResultsApi.getDetails());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new Results(400, "There was an issue processing the request");

        }


    }

    @Override
    public Results unsubscribe(DbNotificationSub notificationSubscription) {
        String internationalBaseApi = AppConstants.INTERNATIONAL_NOTIFICATION +"unsubscribe-email";
        return getPostResults(notificationSubscription, internationalBaseApi);
    }

    @NotNull
    private Results getIntResults(String internationalBaseApi) {
        try{

            DbResultsApi dbResultsApi = GenericWebclient.getForSingleObjResponse(
                    internationalBaseApi, DbResultsApi.class);
            if (dbResultsApi == null)
                return new Results(400, "There was an issue processing the request");

            if (dbResultsApi.getCode() == 200) {
                return new Results(200, dbResultsApi.getDetails());
            }else {
                return new Results(400, dbResultsApi.getDetails());
            }

        }catch (Exception e){
            e.printStackTrace();
            return new Results(400, "There was an issue processing the request");

        }
    }

    @Override
    public Results getNotifications(int no, int size, String emailAddress) {
        String internationalBaseApi = AppConstants.INTERNATIONAL_NOTIFICATION +"list?email="+emailAddress;
        return getIntResults(internationalBaseApi);
    }

    public Results getSubscriptionDetails(String userId) {
        String apiUrl = AppConstants.INTERNATIONAL_NOTIFICATION + "subscription-details";

        try {
            NotificationSubscription notificationSubscription = webClient.get()
                    .uri(apiUrl + "?userId={userId}", userId)
                    .retrieve()
                    .bodyToMono(NotificationSubscription.class)
                    .block();

            if (notificationSubscription == null) {
                return new Results(400, "There was an issue processing the request");
            }
            return new Results(200, notificationSubscription);
        } catch (Exception e) {
            e.printStackTrace();
            return new Results(400, "An error occurred while processing the request");
        }
    }

    public Results updateSubscription(DbNotificationSub dbNotificationSub) {
        String internationalBaseApi = AppConstants.INTERNATIONAL_NOTIFICATION + "update-subscription";

        try {
            WebClient webClient = WebClient.create();

            // Send the update request with the provided DbNotificationSub object
            UnsubscribeResponse response = webClient.put()
                    .uri(internationalBaseApi)
                    .bodyValue(dbNotificationSub)
                    .retrieve()
                    .bodyToMono(UnsubscribeResponse.class)
                    .block();

            if (response != null && response.getCode() == 200 && response.getDetails() != null) {
                NotificationSubscription details = response.getDetails();
                NotificationSubscription notificationSubscription = new NotificationSubscription(
                        details.getId(),
                        details.getFirstName(),
                        details.getLastName(),
                        details.getEmail(),
                        details.getPhone(),
                        details.isActive(),
                        details.getUserId()
                );
                return new Results(200, notificationSubscription);
            } else {
                return new Results(400, "Resource not found, update not successful");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Results(400, "An error occurred while processing the request");
        }
    }


}