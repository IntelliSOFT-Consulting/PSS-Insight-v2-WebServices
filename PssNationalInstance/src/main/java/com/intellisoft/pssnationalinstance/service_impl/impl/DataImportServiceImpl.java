package com.intellisoft.pssnationalinstance.service_impl.impl;


import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.EnvConfig;
import com.intellisoft.pssnationalinstance.service_impl.service.DataImportService;
import com.intellisoft.pssnationalinstance.util.EnvUrlConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DataImportServiceImpl implements DataImportService {

    private final EnvUrlConstants envUrlConstants;
    private final EnvConfig envConfig;

    @Override
    public Results postDataImport(DbDataImport dbDataImport) throws URISyntaxException {

        String program = dbDataImport.getProgram();
        String orgUnit = dbDataImport.getOrgUnit();
        String eventDate = dbDataImport.getEventDate();
        String status = dbDataImport.getStatus();
        String storedBy = dbDataImport.getStoredBy();
        List<DataValue> dataValues = dbDataImport.getDataValues();

        // Create the request object for the external API
        DbDataImport dataImport = new DbDataImport(program, orgUnit, eventDate, status, storedBy, dataValues);

        String authHeader = "Basic " + Base64.getEncoder().encodeToString((envConfig.getValue().getUsername() + ":" + envConfig.getValue().getPassword()).getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("Authorization", authHeader);

        DbEvents response = GenericWebclient.postForSingleObjResponseWithAuth(envUrlConstants.getEVENTS_ENDPOINT(), dataImport, DbDataImport.class, DbEvents.class, authHeader);

        if (response.getHttpStatusCode() == 200) {
            return new Results(200, "Import was successful.");
        }

        return new Results(400, "There was an issue processing your request.");
    }

}
