package com.intellisoft.nationalinstance.service_impl;

import com.intellisoft.nationalinstance.*;
import com.intellisoft.nationalinstance.db.SurveyRespondents;
import com.intellisoft.nationalinstance.db.Surveys;
import com.intellisoft.nationalinstance.db.repso.IndicatorsRepo;
import com.intellisoft.nationalinstance.db.repso.RespondentAnswersRepo;
import com.intellisoft.nationalinstance.db.repso.SurveyRespondentsRepo;
import com.intellisoft.nationalinstance.db.repso.SurveysRepo;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SurveysServiceImpl implements SurveysService{

    private final SurveysRepo surveysRepo;
    private final SurveyRespondentsRepo respondentsRepo;
    private final IndicatorsRepo indicatorsRepo;
    private final RespondentAnswersRepo respondentAnswersRepo;
    private final VersionServiceImpl versionService;

    @Override
    public Results addSurvey(DbSurvey dbSurvey) {

        String surveyName = dbSurvey.getSurveyName();
        String surveyDescription = dbSurvey.getSurveyDescription();
        String status = dbSurvey.getStatus();
        String creatorId = dbSurvey.getCreatorId();
        List<String> indicatorList = dbSurvey.getIndicators();

        Surveys surveys = new Surveys();
        surveys.setName(surveyName);
        surveys.setDescription(surveyDescription);
        surveys.setStatus(status);
        surveys.setCreatorId(creatorId);
        surveys.setIndicators(indicatorList);
        surveysRepo.save(surveys);

        return new Results(201, surveys);
    }

    @Override
    public Results listAdminSurveys(String creatorId) {

        List<Surveys> surveysList = surveysRepo.findAllByCreatorId(creatorId);
        DbResults dbResults = new DbResults(
                surveysList.size(),
                surveysList);

        return new Results(200, dbResults);
    }

    @Override
    public Results listRespondentsSurveys(String creatorId) {
        List<DbSurveyRespondentDetails> dbSurveyRespondentDetailsList = new ArrayList<>();
        List<Surveys> surveysList = surveysRepo.findAllByCreatorId(creatorId);
        for (int i = 0; i < surveysList.size(); i++){

            String surveyId = String.valueOf(surveysList.get(i).getId());
            String surveyName = surveysList.get(i).getName();
            List<SurveyRespondents> respondentsList =
                    respondentsRepo.findAllBySurveyId(surveyId);

            List<DbRespondentDetails> dbRespondentDetailsList = new ArrayList<>();
            for (int j = 0; j< respondentsList.size(); j++){

                String respondentId = String.valueOf(respondentsList.get(j).getId());
                String emailAddress = respondentsList.get(j).getEmailAddress();
                String createdAt = String.valueOf(respondentsList.get(j).getCreatedAt());

                DbRespondentDetails dbRespondentDetails = new DbRespondentDetails(
                        respondentId,
                        emailAddress,
                        createdAt);
                dbRespondentDetailsList.add(dbRespondentDetails);

            }
            DbSurveyRespondentDetails dbSurveyRespondentDetails = new DbSurveyRespondentDetails(
                    surveyId,
                    surveyName,
                    dbRespondentDetailsList);
            dbSurveyRespondentDetailsList.add(dbSurveyRespondentDetails);

        }
        DbResults dbResults = new DbResults(
                dbSurveyRespondentDetailsList.size(),
                dbSurveyRespondentDetailsList);

        return new Results(200, dbResults);
    }

    @Override
    public Results surveyDetails(String id) {

        List<DbFrontendIndicators> indicatorForFrontEnds = new LinkedList<>();
        Optional<Surveys> optionalSurveys =
                surveysRepo.findById(Long.valueOf(id));

        if (optionalSurveys.isPresent()){

            Surveys surveys = optionalSurveys.get();

            List<String> indicatorList = surveys.getIndicators();
            List<String> metaDataList = indicatorsRepo.findMetadataByIndicatorIds(indicatorList);
            try {

                for(int j = 0; j < metaDataList.size(); j++){
                    String s = metaDataList.get(j);
                    JSONObject jsonObject = new JSONObject(s);
                    versionService.getIndicatorGroupings(indicatorForFrontEnds, jsonObject);
                }

            } catch (JSONException e) {
                System.out.println("*****1");
                e.printStackTrace();
            }

            // Create a map to group the indicators by category name
            Map<String, List<DbFrontendIndicators>> groupedByCategory = new HashMap<>();
            for (DbFrontendIndicators indicator : indicatorForFrontEnds) {
                String categoryName = indicator.getCategoryName();
                if (!groupedByCategory.containsKey(categoryName)) {
                    groupedByCategory.put(categoryName, new LinkedList<>());
                }
                groupedByCategory.get(categoryName).add(indicator);
            }

            // Create a new list of DbFrontendCategoryIndicators
            List<DbFrontendCategoryIndicators> categoryIndicatorsList = new LinkedList<>();
            for (String categoryName : groupedByCategory.keySet()) {
                List<DbFrontendIndicators> categoryIndicators = groupedByCategory.get(categoryName);

                DbFrontendCategoryIndicators category = new DbFrontendCategoryIndicators(categoryName, categoryIndicators);
                categoryIndicatorsList.add(category);
            }

            String surveyId = String.valueOf(surveys.getId());
            String name = surveys.getName();
            String description = surveys.getDescription();
            String status = surveys.getStatus();
            String creatorId = surveys.getCreatorId();
            String createdAt = String.valueOf(surveys.getCreatedAt());
            String updatedAt = String.valueOf(surveys.getUpdatedAt());

//            for (int i = 0; i< categoryIndicatorsList.size(); i++){
//
//                List<DbFrontendIndicators> indicatorDataList = categoryIndicatorsList.get(i).getIndicators();
//                for (int j = 0; j < indicatorDataList.size(); j++){
//
//                    List<DbIndicators> dataValueList = indicatorDataList.get(j).getIndicators();
//                    for (int k = 0; k < dataValueList.size(); k++){
//
//                        String indicatorId = dataValueList.get(k).getId();
//                        respondentAnswersRepo.findByIndicatorIdAndRespondentId(indicatorId, )
//                    }
//
//                }
//
//            }

            DbSurveyRespondentsDetails dbSurveyRespondentsDetails = new DbSurveyRespondentsDetails(
                    surveyId, name, description, status, creatorId, createdAt, updatedAt, categoryIndicatorsList);

            return new Results(200, dbSurveyRespondentsDetails);
        }


        return new Results(400, "Resource not found.");

    }

    @Override
    public Surveys surveyDetailsInfo(String id){
        Optional<Surveys> optionalSurveys =
                surveysRepo.findById(Long.valueOf(id));
        return optionalSurveys.orElse(null);
    }

    @Override
    public Results deleteSurvey(Long id) {
        Optional<Surveys> optionalSurveys =
                surveysRepo.findById(id);
        if (optionalSurveys.isPresent()){
            /**
             * TODO: DELETE survey respondents
             */
            String name = optionalSurveys.get().getName();
            surveysRepo.deleteById(id);
            return new Results(200, new DbDetails(
                    name + " has been deleted successfully."
            ));
        }
        return new Results(400, "Survey not found");
    }

    @Override
    public Results updateSurvey(Long id, DbSurvey dbSurvey) {

        Surveys surveys = surveysRepo.findById(id)
                .map(surveysOld ->{
                    surveysOld.setName(dbSurvey.getSurveyName());
                    surveysOld.setDescription(dbSurvey.getSurveyDescription());
                    surveysOld.setStatus(dbSurvey.getStatus());
                    surveysOld.setIndicators(dbSurvey.getIndicators());
                    return surveysRepo.save(surveysOld);
                } ).orElse(null);

        if (surveys != null){
            return new Results(200, new DbDetails("Survey has been updated successfully."));
        }

        return new Results(400, "Survey could not be updated");
    }
}
