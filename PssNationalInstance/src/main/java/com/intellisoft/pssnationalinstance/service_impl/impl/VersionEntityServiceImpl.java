package com.intellisoft.pssnationalinstance.service_impl.impl;

import com.intellisoft.pssnationalinstance.*;
import com.intellisoft.pssnationalinstance.db.VersionEntity;
import com.intellisoft.pssnationalinstance.repository.VersionEntityRepository;
import com.intellisoft.pssnationalinstance.service_impl.service.InternationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.NationalTemplateService;
import com.intellisoft.pssnationalinstance.service_impl.service.VersionEntityService;
import com.intellisoft.pssnationalinstance.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class VersionEntityServiceImpl implements VersionEntityService {

    private final VersionEntityRepository versionEntityRepository;
    private final InternationalTemplateService internationalTemplateService;
    private final NationalTemplateService nationalTemplateService;



    @Override
    public Results addVersion(DbVersions dbVersions) {

        String publishedBaseUrl = AppConstants.NATIONAL_PUBLISHED_VERSIONS;

        String versionDescription = dbVersions.getVersionDescription();
        boolean isPublished = dbVersions.isPublished();
        String createdBy = dbVersions.getCreatedBy();
        String publishedBy = dbVersions.getPublishedBy();
        List<DbVersionDate> dbVersionsIndicators = dbVersions.getIndicators();
        List<String> indicatorList = new ArrayList<>();
        for (DbVersionDate dbVersionDate : dbVersionsIndicators){
            String id = dbVersionDate.getId();
            indicatorList.add(id);
        }

        String status = PublishStatus.DRAFT.name();

        if (publishedBy == null) publishedBy = "";
        if (isPublished) status = PublishStatus.PUBLISHED.name();

        VersionEntity versionEntity = new VersionEntity();
        versionEntity.setVersionDescription(versionDescription);
        versionEntity.setIndicators(indicatorList);
        versionEntity.setCreatedBy(createdBy);
        versionEntity.setStatus(status);
        versionEntity.setPublishedBy(publishedBy);
        VersionEntity savedVersionEntity = versionEntityRepository.save(versionEntity);

        if (isPublished){
            nationalTemplateService.savePublishedVersion(
                    createdBy,
                    String.valueOf(savedVersionEntity.getId()),
                    dbVersionsIndicators);
        }

        return new Results(201, new DbDetails("The version has been saved successfully."));
    }

    @Override
    public Results listVersions(int page, int size, boolean isLatest) {
        String publishedBaseUrl = AppConstants.NATIONAL_PUBLISHED_VERSIONS;
        List<DbVersionDetails> dbVersionDetailsList = new ArrayList<>();

        if(isLatest){
            Optional<VersionEntity> versionEntityOptional = versionEntityRepository
                    .findFirstByStatusOrderByCreatedAtDesc(PublishStatus.PUBLISHED.name());
            if (versionEntityOptional.isPresent()){
                VersionEntity versionEntity = versionEntityOptional.get();

                DbMetadataJson dbMetadataJson =
                        internationalTemplateService.getPublishedData(publishedBaseUrl);
                if (dbMetadataJson != null){
                    DbPrograms metadata = dbMetadataJson.getMetadata();
                    if (metadata != null){
                        DbPublishedVersion publishedData = metadata.getPublishedVersion();
                        if(publishedData != null){
                            DbVersionDetails dbVersionDetails = new DbVersionDetails(
                                    versionEntity.getId(),
                                    versionEntity.getVersionName(),
                                    versionEntity.getVersionDescription(),
                                    versionEntity.getCreatedBy(),
                                    versionEntity.getStatus(),
                                    versionEntity.getPublishedBy(),
                                    String.valueOf(versionEntity.getCreatedAt()),
                                    publishedData);
                            dbVersionDetailsList.add(dbVersionDetails);
                            DbResultsData dbResultsData = new DbResultsData(
                                    dbVersionDetailsList.size(),
                                    dbVersionDetailsList
                            );
                            return new Results(200, dbResultsData);
                        }
                    }
                }

            }
        }


        List<DbIndicators> dbIndicatorsList = new ArrayList<>();
        DbPublishedVersion dbPublishedVersion = getAvailableVersion();
        if (dbPublishedVersion != null){
            dbIndicatorsList = dbPublishedVersion.getDetails();
        }

        List<VersionEntity> versionEntityList = getPagedVersions(
                page, size, "","");
        for (int i = 0; i < versionEntityList.size(); i++){
            Long id = versionEntityList.get(i).getId();
            String versionName = versionEntityList.get(i).getVersionName();
            String versionDescription = versionEntityList.get(i).getVersionDescription();
            String createdBy = versionEntityList.get(i).getCreatedBy();
            String publishedBy = versionEntityList.get(i).getPublishedBy();
            String status = versionEntityList.get(i).getStatus();
            String createdAt = String.valueOf(versionEntityList.get(i).getCreatedAt());
            List<String> indicatorList = versionEntityList.get(i).getIndicators();
            List<DbIndicators> selectedIndicators =
                    nationalTemplateService.getSelectedIndicators(dbIndicatorsList, indicatorList);
            DbVersionDetails dbVersionDetails = new DbVersionDetails(
                    id,
                    versionName,
                    versionDescription,
                    createdBy,
                    status,
                    publishedBy,
                    createdAt,
                    selectedIndicators);
            dbVersionDetailsList.add(dbVersionDetails);

        }

        DbResultsData dbResultsData = new DbResultsData(
                dbVersionDetailsList.size(),
                dbVersionDetailsList
        );
        return new Results(200, dbResultsData);
    }

    @Override
    public Results updateVersion(String id, DbVersions dbVersions) {

        String description = dbVersions.getVersionDescription();
        List<DbVersionDate> dbVersionsIndicators = dbVersions.getIndicators();
        boolean isPublished = dbVersions.isPublished();

        List<String> indicatorList = new ArrayList<>();
        for (DbVersionDate dbVersionDate : dbVersionsIndicators){
            String indicatorId = dbVersionDate.getId();
            indicatorList.add(indicatorId);
        }

        Optional<VersionEntity> optionalVersionEntity =
                versionEntityRepository.findById(Long.valueOf(id));
        if(optionalVersionEntity.isPresent()){
            VersionEntity versionEntity = optionalVersionEntity.get();
            String status = versionEntity.getStatus();
            if (status.equals(PublishStatus.PUBLISHED.name())){
                return new Results(400, "You cannot edit a published version");
            }
            //Update database
            versionEntity.setVersionDescription(description);
            versionEntity.setIndicators(indicatorList);
            if (isPublished){
                versionEntity.setStatus(PublishStatus.PUBLISHED.name());
                nationalTemplateService.savePublishedVersion(
                        versionEntity.getCreatedBy(),
                        id,
                        dbVersionsIndicators);
            }
            versionEntityRepository.save(versionEntity);
            return new Results(200, versionEntity);
        }

        return new Results(400, "We could not find that resource.");

    }

    @Override
    public Results deleteTemplate(String versionId) {

        try{
            Optional<VersionEntity> optionalVersionEntity = versionEntityRepository.findById(Long.valueOf(versionId));
            if (optionalVersionEntity.isPresent()){

                VersionEntity versionEntity = optionalVersionEntity.get();
                String status = versionEntity.getStatus();
                if (status.equals(PublishStatus.PUBLISHED.name())){
                    return new Results(400, "You cannot delete a published version");
                }

                versionEntityRepository.deleteById(versionEntity.getId());
                return new Results(200, new DbDetails("The version has been deleted successfully."));

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return new Results(400, "Version not found.");
    }

    @Override
    public Results getVersionDetails(String versionId) {

        try{
            DbPublishedVersion dbPublishedVersion;

            Optional<VersionEntity> optionalVersionEntity = versionEntityRepository.findById(Long.valueOf(versionId));
            if (optionalVersionEntity.isPresent()){
                VersionEntity versionEntity = optionalVersionEntity.get();
                String versionNo = versionEntity.getVersionName();
                if (versionNo != null){
                    dbPublishedVersion = getThePreviousIndicators(versionNo);
                }else {
                    dbPublishedVersion = getAvailableVersion();
                }

                DbVersionDataDetails dbVersionDataDetails = new DbVersionDataDetails(
                        versionEntity.getId(),
                        versionEntity.getVersionName(),
                        versionEntity.getVersionDescription(),
                        versionEntity.getStatus(),
                        versionEntity.getCreatedBy(),
                        versionEntity.getPublishedBy(),
                        null);

                List<String> indicators = versionEntity.getIndicators();

                DbPublishedVersion publishedVersion = filterDbPublishedVersionByCategoryId(indicators, dbPublishedVersion);
                dbVersionDataDetails.setIndicators(publishedVersion);
                return new Results(200, dbVersionDataDetails);

            }


        }catch (Exception e){
            e.printStackTrace();
        }

        return new Results(400, "Resource not found.");
    }
    public static DbPublishedVersion filterDbPublishedVersionByCategoryId(List<String> categoryIds, DbPublishedVersion version) {
        List<DbIndicators> filteredDetails = version.getDetails().stream()
                .map(indicator -> {
                    List<DbIndicatorValues> filteredIndicators = indicator.getIndicators().stream()
                            .filter(indicatorValue -> indicatorValue != null
                                    && categoryIds.contains(indicatorValue.getCategoryId().toString()))
                            .collect(Collectors.toList());
                    if (!filteredIndicators.isEmpty()) {
                        return new DbIndicators(indicator.getCategoryName(), filteredIndicators);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return new DbPublishedVersion(filteredDetails.size(), filteredDetails);
    }


    private DbPublishedVersion getThePreviousIndicators(String versionNumber){
        String publishedBaseUrl = AppConstants.NATIONAL_PUBLISHED_VERSIONS + versionNumber;
        DbMetadataJson dbMetadataJson =
                internationalTemplateService.getIndicators(publishedBaseUrl);

        if (dbMetadataJson != null){
            DbPrograms dbPrograms = dbMetadataJson.getMetadata();
            if (dbPrograms != null){
                return dbPrograms.getPublishedVersion();
            }
        }
        return null;
    }


    private DbPublishedVersion getAvailableVersion(){
        DbPublishedVersion dbPublishedVersion =
                nationalTemplateService.nationalPublishedIndicators();
        if (dbPublishedVersion != null){
            return dbPublishedVersion;
        }else {
            return internationalTemplateService.interNationalPublishedIndicators();
        }
    }

    private List<VersionEntity> getPagedVersions(
            int pageNo,
            int pageSize,
            String sortField,
            String sortDirection) {
        String sortPageField = "";
        String sortPageDirection = "";

        if (sortField.equals("")){sortPageField = "createdAt"; }else {sortPageField = sortField;}
        if (sortDirection.equals("")){sortPageDirection = "DESC"; }else {sortPageDirection = sortField;}

        Sort sort = sortPageDirection.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortPageField).ascending() : Sort.by(sortPageField).descending();
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<VersionEntity> page = versionEntityRepository.findAll(pageable);

        return page.getContent();
    }
}
