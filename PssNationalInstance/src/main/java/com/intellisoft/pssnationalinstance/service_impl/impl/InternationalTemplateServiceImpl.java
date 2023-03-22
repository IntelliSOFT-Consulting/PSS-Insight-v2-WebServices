package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.service_impl.service.InternationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.NationalTemplateService;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class InternationalTemplateServiceImpl implements InternationalTemplateService {

    private final FormatterClass formatterClass = new FormatterClass();


    @Override
    public Results getInternationalIndicators() {

        try{

            DbTemplates dbTemplates = new DbTemplates();

            DbPublishedVersion interNationalPublishedIndicators =
                    interNationalPublishedIndicators();
            if (interNationalPublishedIndicators != null){
                String publishedBaseUrl = AppConstants.INTERNATIONAL_PUBLISHED_VERSIONS;

                int versionNo = getVersions(publishedBaseUrl);
                DbTemplateDetails dbTemplateDetails =
                        new DbTemplateDetails(versionNo, interNationalPublishedIndicators);
                dbTemplates.setInterNationalTemplate(dbTemplateDetails);
            }

            String publishedBaseUrl = AppConstants.NATIONAL_PUBLISHED_VERSIONS;
            DbMetadataJson dbMetadataJson = getPublishedData(publishedBaseUrl);
            if (dbMetadataJson != null){
                DbPrograms dbPrograms = dbMetadataJson.getMetadata();
                if (dbPrograms != null){
                    DbPublishedVersion nationalPublishedIndicators =
                            dbPrograms.getPublishedVersion();
                    if (nationalPublishedIndicators != null){
                        int versionNo = getVersions(publishedBaseUrl);
                        DbTemplateDetails dbTemplateDetails =
                                new DbTemplateDetails(versionNo, interNationalPublishedIndicators);
                        dbTemplates.setNationalTemplate(dbTemplateDetails);
                    }
                }
            }

            return new Results(200, dbTemplates);

        } catch (Exception syntaxException){
            syntaxException.printStackTrace();
        }

        return new Results(400, "The international indicators could not be found.");

    }

    public DbPublishedVersion interNationalPublishedIndicators(){
        String publishedBaseUrl = AppConstants.INTERNATIONAL_PUBLISHED_VERSIONS;
        DbMetadataJson dbMetadataJson = getPublishedData(publishedBaseUrl);
        if (dbMetadataJson != null){
            DbPrograms dbPrograms = dbMetadataJson.getMetadata();
            if (dbPrograms != null){
                return dbPrograms.getPublishedVersion();
            }
        }
        return null;
    }

    public int getVersions(String url) throws URISyntaxException {
        var response = GenericWebclient.getForSingleObjResponse(
                url,
                List.class);
        if (!response.isEmpty()){
            return formatterClass.getNextVersion(response);
        }else {
            return 1;
        }
    }

    public DbMetadataJson getPublishedData(String url) {

        try {
            //Get latest international version
            int publishedVersionNo = getVersions(url);

            //Get the dataStore values from the international
            DbMetadataJson dbMetadataJson = GenericWebclient.getForSingleObjResponse(
                    url+publishedVersionNo, DbMetadataJson.class);

            if (dbMetadataJson.getMetadata() != null){
                return dbMetadataJson;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }
}
