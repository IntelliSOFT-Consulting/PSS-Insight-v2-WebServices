package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.db.Benchmarks;
import com.intellisoft.pssnationalinstance.repository.BenchmarksRepository;
import com.intellisoft.pssnationalinstance.service_impl.service.InternationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.NationalTemplateService;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class InternationalTemplateServiceImpl implements InternationalTemplateService {

    private final FormatterClass formatterClass = new FormatterClass();
    private final BenchmarksRepository benchmarksRepository;


    private static List<IndicatorBenchmark> getIndicatorCodesFromBenchmarksAPI() throws URISyntaxException {
        String url = AppConstants.FETCH_BENCHMARKS_API;
        Flux<IndicatorBenchmark> responseFlux = GenericWebclient.getForCollectionResponse(url, IndicatorBenchmark.class);
        List<IndicatorBenchmark> benchmarkList = responseFlux.collectList().block();

        if (benchmarkList != null) {
            List<IndicatorBenchmark> indicatorBenchmarks = new ArrayList<>();
            for (IndicatorBenchmark benchmarks : benchmarkList) {
                String Benchmark = (String) benchmarks.getBenchmark();
                String Indicator_Code = (String) benchmarks.getIndicatorCode();
                indicatorBenchmarks.add(benchmarks);
            }
            return indicatorBenchmarks;
        }
        return Collections.emptyList();
    }

    public Results getInternationalIndicators() {
        try {
            List<DbTemplateDetails> dbTemplateDetailsList = new ArrayList<>();
            String publishedBaseUrl = AppConstants.INTERNATIONAL_PUBLISHED_VERSIONS;

            DbPublishedVersion interNationalPublishedIndicators = interNationalPublishedIndicators();
            if (interNationalPublishedIndicators != null) {
                int versionNo = getVersions(publishedBaseUrl);
                DbTemplateDetails dbTemplateDetails = new DbTemplateDetails(versionNo, getInterNationalPublishedIndicators());
                dbTemplateDetailsList.add(dbTemplateDetails);
            }

            DbTemplateDetails dbTemplateDetails = getRecentPublishedData(publishedBaseUrl);
            dbTemplateDetailsList.add(dbTemplateDetails);

            List<IndicatorBenchmark> indicatorCodes = getIndicatorCodesFromBenchmarksAPI();

            for (DbTemplateDetails dbTemplateDetailsItem : dbTemplateDetailsList) {
                Object indicators = dbTemplateDetailsItem.getIndicators();
                if (indicators instanceof DbPublishedVersionDetails) {
                    DbPublishedVersionDetails publishedVersionDetails = (DbPublishedVersionDetails) indicators;
                    List<DbIndicators> details = publishedVersionDetails.getDetails();
                    for (DbIndicators detail : details) {
                        Object categoryName = detail.getCategoryName();
                        List<DbIndicatorValues> indicatorValues = detail.getIndicators();
                        for (DbIndicatorValues indicatorValue : indicatorValues) {
                            Object indicatorCategoryName = indicatorValue.getCategoryName();
                            if (indicatorCategoryName != null && indicatorCategoryName instanceof String) {


                                Optional<Benchmarks> optionalBenchmarks = benchmarksRepository.findByIndicatorCode(indicatorCategoryName);
                                if (optionalBenchmarks.isPresent()) {
                                    Benchmarks benchmarks = optionalBenchmarks.get();
                                    String benchmarkValue = benchmarks.getValue();
                                    List<DbIndicatorDataValues> indicatorDataValues = indicatorValue.getIndicatorDataValue();
                                    for (DbIndicatorDataValues indicatorDataValue : indicatorDataValues) {
//                                        indicatorDataValue.setBenchmark(benchmarkValue);
//                                        indicatorDataValue.setInternationalBenchmark(benchmarkValue);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return new Results(200, dbTemplateDetailsList);
        } catch (Exception syntaxException) {
            syntaxException.printStackTrace();
        }
        return new Results(400, "The international indicators could not be found.");
    }


    public DbPublishedVersionDetails getInterNationalPublishedIndicators(){
        String publishedBaseUrl = AppConstants.INTERNATIONAL_PUBLISHED_VERSIONS;
        DbMetadataJson dbMetadataJson = getPublishedData(publishedBaseUrl);
        if (dbMetadataJson != null){
            DbPrograms dbPrograms = dbMetadataJson.getMetadata();
            if (dbPrograms != null){
                String url = (String) dbPrograms.getReferenceSheet();


                return new DbPublishedVersionDetails(
                        null, null,
                        url,
                        dbPrograms.getPublishedVersion().getCount(),
                        dbPrograms.getPublishedVersion().getDetails()
                );
            }
        }
        return null;
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

    private DbTemplateDetails getRecentPublishedData(String url){
        try {
            //Get latest international version
            int publishedVersionNo = getVersions(url);
            if (publishedVersionNo > 1){
                //Get the dataStore values from the international
                int recentVersionNo = publishedVersionNo - 1;

                DbMetadataJson dbMetadataJson = GenericWebclient.getForSingleObjResponse(
                        url+recentVersionNo, DbMetadataJson.class);
                if (dbMetadataJson.getMetadata() != null){

                    DbPublishedVersion indicators = dbMetadataJson.getMetadata().getPublishedVersion();
                    String refSheet = (String) dbMetadataJson.getMetadata().getReferenceSheet();

                    DbPublishedVersionDetails dbPublishedVersionDetails = new DbPublishedVersionDetails(
                            null, null,
                            refSheet,
                            indicators.getCount(),
                            indicators.getDetails());
                    return new DbTemplateDetails(
                            recentVersionNo,
                            dbPublishedVersionDetails);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
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
    public DbMetadataJsonNational getPublishedDataNational(String url) {

        try {
            //Get latest international version
            int publishedVersionNo = getVersions(url);

            //Get the dataStore values from the international
            DbMetadataJsonNational dbMetadataJson = GenericWebclient.getForSingleObjResponse(
                    url+publishedVersionNo, DbMetadataJsonNational.class);

            if (dbMetadataJson.getMetadata() != null){
                return dbMetadataJson;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

    public DbMetadataJson getIndicators(String url) {

        try {

            //Get the dataStore values from the international
            DbMetadataJson dbMetadataJson = GenericWebclient.getForSingleObjResponse(
                    url, DbMetadataJson.class);

            if (dbMetadataJson.getMetadata() != null){
                return dbMetadataJson;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }
}
