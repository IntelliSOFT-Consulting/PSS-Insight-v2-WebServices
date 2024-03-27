package com.intellisoft.internationalinstance.service_impl.impl;

import com.google.common.collect.Lists;
import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.db.VersionEntity;
import com.intellisoft.internationalinstance.db.repso.VersionRepos;
import com.intellisoft.internationalinstance.service_impl.service.InternationalService;
import com.intellisoft.internationalinstance.service_impl.service.VersionService;
import com.intellisoft.internationalinstance.util.AppConstants;
import com.intellisoft.internationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {
    private final VersionRepos versionRepos;
    private final InternationalService internationalService;

    @Value("${dhis.username}")
    private String username;
    @Value("${dhis.password}")
    private String password;
    @Value("${dhis.international}")
    private String dhisInternationalUrl;

    @Value("${dhis.template}")
    private String dhisTemplate;

    @Override
    public Results getTemplates(int page, int size, String status) {

        List<VersionEntity> versionEntityList = getPagedList(page, size, "", "", status);


        List<DbVersionDetails> dbVersionDetailsList = getSelectedIndicators(versionEntityList);

        DbResults dbResults = new DbResults((int) versionRepos.count(), dbVersionDetailsList);

        return new Results(200, dbResults);
    }

    private List<VersionEntity> getPagedList(int pageNo, int pageSize, String sortField, String sortDirection, String status) {
        String sortPageField = "";
        String sortPageDirection = "";

        if (sortField.equals("")) {
            sortPageField = "createdAt";
        } else {
            sortPageField = sortField;
        }
        if (sortDirection.equals("")) {
            sortPageDirection = "DESC";
        } else {
            sortPageDirection = sortField;
        }

        Sort sort = sortPageDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortPageField).ascending() : Sort.by(sortPageField).descending();
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<VersionEntity> page;
        if (status.equals("ALL")) {
            page = versionRepos.findAll(pageable);
        } else {
            page = versionRepos.findAllByStatus(status, pageable);
        }

        return page.getContent();
    }

    private List<DbVersionDetails> getSelectedIndicators(List<VersionEntity> versionEntityList) {

        List<DbVersionDetails> dbVersionDetailsList = new ArrayList<>();

        List<DbIndicatorsValue> indicatorsValueList = internationalService.getIndicatorsValues();

        for (VersionEntity versionEntity : versionEntityList) {

            List<DbIndicatorsValue> dbIndicatorsValueList = new ArrayList<>();
            List<String> indicatorList = versionEntity.getIndicators();
            for (DbIndicatorsValue dbIndicatorsValue : indicatorsValueList) {

                List<DbIndicatorDataValues> neDbIndicatorDataValuesList = new ArrayList<>();
                String categoryName = (String) dbIndicatorsValue.getCategoryName();

                List<DbIndicatorDataValues> dbIndicatorDataValuesList = dbIndicatorsValue.getIndicators();
                for (DbIndicatorDataValues dataValues : dbIndicatorDataValuesList) {
                    String categoryId = (String) dataValues.getCategoryId();
                    if (indicatorList.contains(categoryId)) {
                        neDbIndicatorDataValuesList.add(dataValues);
                    }
                }

                if (!neDbIndicatorDataValuesList.isEmpty()) {
                    DbIndicatorsValue dbIndicatorsValueNew = new DbIndicatorsValue(categoryName, neDbIndicatorDataValuesList);
                    dbIndicatorsValueList.add(dbIndicatorsValueNew);
                }


            }

            String versionName = versionEntity.getVersionName();
            String url = (dhisInternationalUrl != null && !dhisInternationalUrl.isEmpty() ? dhisInternationalUrl + "/api/" : "https://global.pssinsight.org/api/") + "dataStore/"+dhisTemplate+"/" + versionName;
            DbMetadataJson dbMetadataJson = getIndicators(url);
            String referenceSheet = "";
            if (dbMetadataJson != null) referenceSheet = (String) dbMetadataJson.getMetadata().getReferenceSheet();


            DbVersionDetails details = new DbVersionDetails(versionEntity.getId(), versionName, versionEntity.getVersionDescription(), versionEntity.getStatus(), versionEntity.getCreatedBy(), versionEntity.getPublishedBy(), versionEntity.getCreatedAt(), dbIndicatorsValueList, referenceSheet);


            dbVersionDetailsList.add(details);

        }

        return dbVersionDetailsList;

    }

    public DbMetadataJson getIndicators(String url) {

        try {

            //Auth-headers:
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);

            DbMetadataJson dbMetadataJson = WebClient.builder().baseUrl(url).defaultHeader(HttpHeaders.AUTHORIZATION, authHeader).exchangeStrategies(ExchangeStrategies.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                    .build()).build().get().retrieve().bodyToMono(DbMetadataJson.class).block();

            if (dbMetadataJson != null) {
                dbMetadataJson.getMetadata();
            }
            return dbMetadataJson;
        } catch (Exception e) {
            log.error("Error occurred while fetching indicators");
        }
        return null;

    }

    @Override
    public Results deleteTemplate(long deleteId) {

        Results results;

        Optional<VersionEntity> optionalVersionEntity = versionRepos.findById(deleteId);
        if (optionalVersionEntity.isPresent()) {

            VersionEntity versionEntity = optionalVersionEntity.get();
            String status = versionEntity.getStatus();
            if (status.equals(PublishStatus.PUBLISHED.name())) {
                return new Results(400, "You cannot delete a published version.");
            }

            versionRepos.deleteById(deleteId);
            results = new Results(200, new DbDetails(optionalVersionEntity.get().getVersionName() + " has been deleted successfully."));
        } else {
            results = new Results(400, "The id cannot be found.");
        }


        return results;
    }

    @Override
    public Results getVersion(long versionId) {

        Results results;

        Optional<VersionEntity> optionalVersionEntity = versionRepos.findById(versionId);


        if (optionalVersionEntity.isPresent()) {
            List<VersionEntity> versionEntityList = new ArrayList<>();
            VersionEntity versionEntity = optionalVersionEntity.get();
            versionEntityList.add(versionEntity);

            List<DbVersionDetails> dbVersionDetailsList = getSelectedIndicators(versionEntityList);

            results = new Results(200, dbVersionDetailsList);

        } else {
            results = new Results(400, "Version could not be found.");
        }
        return results;
    }


}
