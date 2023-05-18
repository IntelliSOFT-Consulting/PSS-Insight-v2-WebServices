package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.DbMetadataJson;
import com.intellisoft.pssnationalinstance.DbNotificationSub;
import com.intellisoft.pssnationalinstance.DbResultsApi;
import com.intellisoft.pssnationalinstance.Results;
import com.intellisoft.pssnationalinstance.service_impl.service.NotificationService;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService {



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
    public Results unsubscribe(String email) {
        String internationalBaseApi = AppConstants.INTERNATIONAL_NOTIFICATION +"unsubscribe-email";
        DbNotificationSub dbNotificationSub = new DbNotificationSub(
                null,
                null,
                null,
                email,
                null
        );
        return getPostResults(dbNotificationSub, internationalBaseApi);
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
}
