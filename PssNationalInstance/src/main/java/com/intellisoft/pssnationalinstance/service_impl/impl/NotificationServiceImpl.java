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

        return getIntResults(internationalBaseApi);
    }

    @Override
    public Results unsubscribe(String email) {
        String internationalBaseApi = AppConstants.INTERNATIONAL_NOTIFICATION +"unsubscribe?email="+email;
        return getIntResults(internationalBaseApi);
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
    public Results getNotifications(int no, int size, String status, String emailAddress) {
        return null;
    }
}
