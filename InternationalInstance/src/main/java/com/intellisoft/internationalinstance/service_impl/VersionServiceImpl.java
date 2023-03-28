package com.intellisoft.internationalinstance.service_impl;

import com.google.common.collect.Lists;
import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.db.Indicators;
import com.intellisoft.internationalinstance.db.MetadataJson;
import com.intellisoft.internationalinstance.db.VersionEntity;
import com.intellisoft.internationalinstance.db.repso.IndicatorsRepo;
import com.intellisoft.internationalinstance.db.repso.VersionRepos;
import com.intellisoft.internationalinstance.exception.CustomException;
import com.intellisoft.internationalinstance.model.Response;
import com.intellisoft.internationalinstance.util.AppConstants;
import com.intellisoft.internationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.net.URISyntaxException;
import java.util.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class VersionServiceImpl implements VersionService {
    private final IndicatorsRepo indicatorsRepo;
    private final VersionRepos versionRepos;
    private final FormatterClass formatterClass = new FormatterClass();
    private final InternationalService internationalService;


    @Override
    public Results getTemplates(int page, int size, String status) {

        List<VersionEntity> versionEntityList;
        if (status.equals("ALL")){
            versionEntityList = versionRepos.findAll();
        }else {
            versionEntityList = versionRepos.findByStatus(status);
        }


        List<DbVersionDetails> dbVersionDetailsList = getSelectedIndicators(versionEntityList);

        DbResults dbResults = new DbResults(
                dbVersionDetailsList.size(),
                dbVersionDetailsList);

        return new Results(200, dbResults);
    }

    private List<DbVersionDetails> getSelectedIndicators(List<VersionEntity> versionEntityList){

        List<DbVersionDetails> dbVersionDetailsList = new ArrayList<>();

        List<DbIndicatorsValue> indicatorsValueList =
                internationalService.getIndicatorsValues();

        for (VersionEntity versionEntity: versionEntityList){

            List<DbIndicatorsValue> dbIndicatorsValueList = new ArrayList<>();
            List<String> indicatorList = versionEntity.getIndicators();
            for (DbIndicatorsValue dbIndicatorsValue : indicatorsValueList){

                List<DbIndicatorDataValues> neDbIndicatorDataValuesList = new ArrayList<>();
                String categoryName = (String) dbIndicatorsValue.getCategoryName();

                List<DbIndicatorDataValues> dbIndicatorDataValuesList = dbIndicatorsValue.getIndicators();
                for (DbIndicatorDataValues dataValues :dbIndicatorDataValuesList){
                    String  categoryId = (String) dataValues.getCategoryId();
                    if (indicatorList.contains(categoryId)){
                        neDbIndicatorDataValuesList.add(dataValues);
                    }
                }

                if (!neDbIndicatorDataValuesList.isEmpty()){
                    DbIndicatorsValue dbIndicatorsValueNew = new DbIndicatorsValue(
                            categoryName, neDbIndicatorDataValuesList
                    );
                    dbIndicatorsValueList.add(dbIndicatorsValueNew);
                }


            }

            DbVersionDetails details = new DbVersionDetails(
                    versionEntity.getId(),
                    versionEntity.getVersionName(),
                    versionEntity.getVersionDescription(),
                    versionEntity.getStatus(),
                    versionEntity.getCreatedBy(),
                    versionEntity.getPublishedBy(),
                    versionEntity.getCreatedAt(),
                    dbIndicatorsValueList);
            dbVersionDetailsList.add(details);

        }

        return dbVersionDetailsList;

    }

    @Override
    public Results deleteTemplate(long deleteId) {

        Results results;

        Optional<VersionEntity> optionalVersionEntity =
                versionRepos.findById(deleteId);
        if (optionalVersionEntity.isPresent()){

            VersionEntity versionEntity = optionalVersionEntity.get();
            String status = versionEntity.getStatus();
            if (status.equals(PublishStatus.PUBLISHED.name())){
                return new Results(400, "You cannot delete a published version.");
            }

            versionRepos.deleteById(deleteId);
            results = new Results(200, new DbDetails(
                    optionalVersionEntity.get().getVersionName() + " has been deleted successfully."
            ));
        }else {
            results = new Results(400, "The id cannot be found.");
        }


        return results;
    }

    @Override
    public Results getVersion(long versionId) {

        Results results;

        Optional<VersionEntity> optionalVersionEntity =
                versionRepos.findById(versionId);


        if (optionalVersionEntity.isPresent()){
            List<VersionEntity> versionEntityList = new ArrayList<>();
            VersionEntity versionEntity = optionalVersionEntity.get();
            versionEntityList.add(versionEntity);

            List<DbVersionDetails> dbVersionDetailsList = getSelectedIndicators(versionEntityList);

            results = new Results(200, dbVersionDetailsList);

        }else {
            results = new Results(400, "Version could not be found.");
        }
        return results;
    }


    private List<VersionEntity> getPagedTemplates(
            int pageNo,
            int pageSize,
            String status
    ) {

        System.out.println("______" + status);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<VersionEntity> page = versionRepos.findByStatus(status, pageable);

        return page.getContent();
    }

    /**
     * Get data from MASTER TEMPLATE from DHIS Datastore and save into local db
     * @return
     * @throws URISyntaxException
     */
    private List<Indicators> getDataFromRemote() throws URISyntaxException {

        List<Indicators> indicators = new LinkedList<>();

        var  res =GenericWebclient.getForSingleObjResponse(
                AppConstants.METADATA_GROUPINGS, String.class);

        JSONObject jsObject = new JSONObject(res);
//        JSONArray dataElements = jsObject.getJSONArray("dataElements");
        JSONArray dataElements = jsObject.getJSONArray("dataElementGroups");
        dataElements.forEach(element->{
            String  id = ((JSONObject)element).getString("id");

            Indicators indicator = new Indicators();
            indicator.setIndicatorId(id);
            indicator.setMetadata(element.toString());

            boolean isIndicator = indicatorsRepo.existsByIndicatorId(id);
            if (!isIndicator){
                indicators.add(indicator);
            }

        });


        return Lists.newArrayList(indicatorsRepo.saveAll(indicators));

    }

}
