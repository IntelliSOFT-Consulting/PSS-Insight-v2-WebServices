package com.intellisoft.pssnationalinstance.service_impl.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.service_impl.service.DataImportService;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.net.URISyntaxException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DataImportServiceImpl implements DataImportService {
    @Override
    public Results postDataImport(DbDataImport dbDataImport) throws JsonProcessingException, URISyntaxException {

        String program = dbDataImport.getProgram();
        String orgUnit = dbDataImport.getOrgUnit();
        String eventDate = dbDataImport.getEventDate();
        String status = dbDataImport.getStatus();
        String storedBy = dbDataImport.getStoredBy();
        List<DataValue> dataValues = dbDataImport.getDataValues();

        // Create the request object for the external API
        DbDataImport dataImport = new DbDataImport(program, orgUnit, eventDate, status, storedBy, dataValues);

        DbEvents response = GenericWebclient.postForSingleObjResponse(AppConstants.EVENTS_ENDPOINT, dataImport, DbDataImport.class, DbEvents.class);

        if (response.getHttpStatusCode() == 200) {
            return new Results(200, "Import was successful.");
        }

        return new Results(400, "There was an issue processing your request.");
    }

}
