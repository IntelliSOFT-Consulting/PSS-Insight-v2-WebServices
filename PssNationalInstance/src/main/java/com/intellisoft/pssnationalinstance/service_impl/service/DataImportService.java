package com.intellisoft.pssnationalinstance.service_impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intellisoft.pssnationalinstance.DbDataImport;
import com.intellisoft.pssnationalinstance.Results;

import javax.validation.Valid;
import java.net.URISyntaxException;

public interface DataImportService {
    Results postDataImport(DbDataImport dbDataImport) throws JsonProcessingException, URISyntaxException;
}
