package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.EnvConfig;
import com.intellisoft.pssnationalinstance.db.Benchmarks;
import com.intellisoft.pssnationalinstance.repository.BenchmarksRepository;
import com.intellisoft.pssnationalinstance.service_impl.service.InternationalTemplateService;
import com.intellisoft.pssnationalinstance.util.EnvUrlConstants;
import com.intellisoft.pssnationalinstance.util.GenericWebclient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

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
    private final EnvUrlConstants envUrlConstants;
    private final EnvConfig envConfig;

    @Value("${dhis.international}")
    private String dhisInternationalUrl;

    private List<IndicatorBenchmark> getIndicatorCodesFromBenchmarksAPI() {
        String url = envUrlConstants.getFETCH_BENCHMARKS_API();
        Flux<IndicatorBenchmark> responseFlux = WebClient.builder().baseUrl(url).defaultHeaders(headers -> headers.addAll(getHeaders().getHeaders())).build().get().retrieve().bodyToFlux(IndicatorBenchmark.class);
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

    private HttpEntity<String> getHeaders() {

        String username = envConfig.getValue().getUsername();
        String password = envConfig.getValue().getPassword();

        String auth = username + ":" + password;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + Base64Utils.encodeToString(auth.getBytes()));

        return new HttpEntity<>(headers);

    }

    public Results getInternationalIndicators() {
        try {
            List<DbTemplateDetails> dbTemplateDetailsList = new ArrayList<>();
            String publishedBaseUrl = envUrlConstants.getINTERNATIONAL_PUBLISHED_VERSIONS();

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

                                List<DbIndicatorDataValues> indicatorDataValues = indicatorValue.getIndicatorDataValue();
                                for (DbIndicatorDataValues indicatorDataValue : indicatorDataValues) {
                                    String categoryCode = (String) indicatorDataValue.getCode();
                                    boolean stringsMatch = indicatorCategoryName.equals(categoryCode);

                                    if (stringsMatch) {
                                        Optional<Benchmarks> optionalBenchmarks = benchmarksRepository.findFirstByIndicatorCode(categoryCode);
                                        if (optionalBenchmarks.isPresent()) {
                                            Benchmarks benchmarks = optionalBenchmarks.get();
                                            String nationalValue = benchmarks.getNationalValue();
                                            String internationalValue = benchmarks.getValue();
                                            indicatorValue.setBenchmark(nationalValue);
                                            indicatorValue.setInternationalBenchmark(internationalValue);
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
            return new Results(200, dbTemplateDetailsList);
        } catch (Exception syntaxException) {
            log.error("An error occurred while fetching international indicators {}", syntaxException.getMessage());
        }
        return new Results(400, "The international indicators could not be found.");
    }


    public DbPublishedVersionDetails getInterNationalPublishedIndicators() {
        String publishedBaseUrl = envUrlConstants.getINTERNATIONAL_PUBLISHED_VERSIONS();
        DbMetadataJson dbMetadataJson = getPublishedData(publishedBaseUrl);
        if (dbMetadataJson != null) {
            DbPrograms dbPrograms = dbMetadataJson.getMetadata();
            if (dbPrograms != null) {
                String url = (String) dbPrograms.getReferenceSheet();


                return new DbPublishedVersionDetails(null, null, url, dbPrograms.getPublishedVersion().getCount(), dbPrograms.getPublishedVersion().getDetails());
            }
        }
        return null;
    }

    public DbPublishedVersion interNationalPublishedIndicators() {
        String publishedBaseUrl = envUrlConstants.getINTERNATIONAL_PUBLISHED_VERSIONS();
        DbMetadataJson dbMetadataJson = getPublishedData(publishedBaseUrl);
        if (dbMetadataJson != null) {
            DbPrograms dbPrograms = dbMetadataJson.getMetadata();
            if (dbPrograms != null) {

                return dbPrograms.getPublishedVersion();
            }
        }
        return null;
    }

    public int getVersions(String url) {
        var response = WebClient.builder().baseUrl(url).defaultHeaders(headers -> headers.addAll(getHeaders().getHeaders())).build().get().retrieve().bodyToMono(List.class).block();

        if (!response.isEmpty()) {
            return formatterClass.getNextVersion(response);
        } else {
            return 1;
        }
    }

    private DbTemplateDetails getRecentPublishedData(String url) {
        try {
            //Get latest international version
            int publishedVersionNo = getVersions(url);
            if (publishedVersionNo > 1) {
                //Get the dataStore values from the international
                int recentVersionNo = publishedVersionNo - 1;

                DbMetadataJson dbMetadataJson = WebClient.builder().baseUrl(url + recentVersionNo).defaultHeaders(headers -> headers.addAll(getHeaders().getHeaders())).exchangeStrategies(ExchangeStrategies.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build()).build().get().retrieve().bodyToMono(DbMetadataJson.class).block();


                if (dbMetadataJson.getMetadata() != null) {

                    DbPublishedVersion indicators = dbMetadataJson.getMetadata().getPublishedVersion();
                    String refSheet = (String) dbMetadataJson.getMetadata().getReferenceSheet();

                    DbPublishedVersionDetails dbPublishedVersionDetails = new DbPublishedVersionDetails(null, null, refSheet, indicators.getCount(), indicators.getDetails());
                    return new DbTemplateDetails(recentVersionNo, dbPublishedVersionDetails);
                }
            }

        } catch (Exception e) {
            log.error("An error occurred when fetching published template");
        }
        return null;
    }

    public DbMetadataJson getPublishedData(String url) {

        try {
            //Get latest international version
            int publishedVersionNo = getVersions(url);

            //Get the dataStore values from the international
            DbMetadataJson dbMetadataJson = WebClient.builder().baseUrl(url + publishedVersionNo).defaultHeaders(headers -> headers.addAll(getHeaders().getHeaders())).exchangeStrategies(ExchangeStrategies.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build()).build().get().retrieve().bodyToMono(DbMetadataJson.class).block();

            if (dbMetadataJson.getMetadata() != null) {
                return dbMetadataJson;
            }
        } catch (Exception e) {
            log.error("An error occurred while fetching metadata");
        }
        return null;

    }

    public DbMetadataJsonNational getPublishedDataNational(String url) {

        try {
            //Get latest international version
            int publishedVersionNo = getVersions(url);

            //Get the dataStore values from the international
            DbMetadataJsonNational dbMetadataJson = WebClient.builder().baseUrl(url + publishedVersionNo).defaultHeaders(headers -> headers.addAll(getHeaders().getHeaders())).exchangeStrategies(ExchangeStrategies.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build()).build().get().retrieve().bodyToMono(DbMetadataJsonNational.class).block();

            if (dbMetadataJson.getMetadata() != null) {
                return dbMetadataJson;
            }
        } catch (Exception e) {
            log.error("An error occurred while fetching metadata");
        }
        return null;

    }

    public DbMetadataJson getIndicators(String url) {

        try {

            //Get the dataStore values from the international
            DbMetadataJson dbMetadataJson = WebClient.builder().baseUrl(url).defaultHeaders(headers -> headers.addAll(getHeaders().getHeaders())).exchangeStrategies(ExchangeStrategies.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build()).build().get().retrieve().bodyToMono(DbMetadataJson.class).block();

            if (dbMetadataJson.getMetadata() != null) {
                return dbMetadataJson;
            }
        } catch (Exception e) {
            log.error("An error occurred while fetching indicators");
        }
        return null;

    }
}