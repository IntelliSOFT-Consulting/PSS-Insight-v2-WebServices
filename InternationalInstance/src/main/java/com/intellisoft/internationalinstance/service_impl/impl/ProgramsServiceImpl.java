package com.intellisoft.internationalinstance.service_impl.impl;

import com.intellisoft.internationalinstance.*;
import com.intellisoft.internationalinstance.service_impl.service.ProgramsService;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ProgramsServiceImpl implements ProgramsService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${dhis.international}")
    private String internationalUrl;

    @Value("${dhis.template}")
    private String master_template;

    @Value("${dhis.username}")
    private String username;
    @Value("${dhis.password}")
    private String password;

    @Value("${dhis.programs}")
    private String programsUrl;
    @Value("${dhis.datastore}")
    private String dataStore;

    private HttpEntity<String> getHeaders(){

        String auth = username + ":" + password;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " +
                Base64Utils.encodeToString(auth.getBytes()));

        return new HttpEntity<>(headers);

    }

    @Override
    public Results programList() {

        Results results;


        ResponseEntity<DbProgramsList> response = restTemplate.exchange(internationalUrl+programsUrl,
                HttpMethod.GET, getHeaders(), DbProgramsList.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            // process the response
            DbProgramsList dbProgramsList = response.getBody();
            if (dbProgramsList != null){
                List<DbPrograms> programsList = dbProgramsList.getPrograms();
                DbResults dbResults = new DbResults(
                        programsList.size(),
                        programsList);
                results = new Results(200, dbResults);
            }else {
                results = new Results(400, new DbDetails("No programs could be found"));
            }
        } else {
            // handle error
            results = new Results(400, new DbDetails("There was an issue getting the programs."));
        }
        return results;
    }

    @Override
    public Results getNamespaces() {


        Results results;

        ResponseEntity<String[]> response = getNamespaceData();
        if (response.getStatusCode() == HttpStatus.OK) {
            // process the response
            String[] list = response.getBody();
            if (list != null){
                DbResults dbResults = new DbResults(
                        list.length,
                        list);
                results = new Results(200, dbResults);
            }else {
                results = new Results(400, new DbDetails("No namespaces could be found"));
            }

        } else {
            // handle error
            results = new Results(400, new DbDetails("There was an issue getting the namespaces."));
        }
        return results;
    }

    private ResponseEntity<String[]> getNamespaceData(){


        String nameSpaceUrl = internationalUrl + dataStore;

        return restTemplate.exchange(nameSpaceUrl,
                HttpMethod.GET, getHeaders(), String[].class);

    }

    @Override
    public Results getVersions(String nameSpace) {

        Results results;

        ResponseEntity<String[]> response = getVersionsData(nameSpace);
        if (response.getStatusCode() == HttpStatus.OK) {
            // process the response
            String[] list = response.getBody();
            if (list != null){
                DbResults dbResults = new DbResults(
                        list.length,
                        list);
                results = new Results(200, dbResults);
            }else {
                results = new Results(400, new DbDetails("No versions could be found under this namespace"));
            }

        } else {
            // handle error
            results = new Results(400, new DbDetails("There was an issue getting the versions."));
        }
        return results;
    }

    private ResponseEntity<String[]> getVersionsData(String namespace){

        String versionUrl = internationalUrl + dataStore + namespace;
        ResponseEntity<String[]> response = restTemplate.exchange(versionUrl,
                HttpMethod.GET, getHeaders(), String[].class);
        return response;
    }

    @Override
    public Results getTemplates(int limitNo) {

        String dataStoreUrl = internationalUrl + dataStore;
        List<DbTemplateData> dbTemplateDataList = new ArrayList<>();

        /**
         * Use the one namespace, get versions under the namespace
         * Using THE namespace and THE version, get the datastore and format
         */
        ResponseEntity<String[]> versionList = getVersionsData(master_template);
        if (versionList.getStatusCode() == HttpStatus.OK ){

            String[] versionDataList = versionList.getBody();
            if (versionDataList != null){

                ArrayList<String> versionValuesList = new ArrayList<>(List.of(versionDataList));
                for (int j = 0; j < versionValuesList.size(); j++){
                    //Get individual version
                    String versionValue = versionValuesList.get(j);
                    String templateUrl = dataStoreUrl + master_template + "/" + versionValue;

                    DbTemplate dbTemplate = getTemplateData(templateUrl);
                    if (dbTemplate != null){

                        String description = "";
                        String program = "";

                        if (dbTemplate.getDescription() != null){
                            description = dbTemplate.getDescription();
                        }
                        if (dbTemplate.getProgram() != null){
                            program = dbTemplate.getProgram();
                        }

                        DbTemplateData dbTemplateData = new DbTemplateData(
                                versionValue,
                                description,
                                program);
                        dbTemplateDataList.add(dbTemplateData);
                    }

                }


            }


        }

        //Truncate the list to the provided limit


        DbResults dbResults = new DbResults(
                dbTemplateDataList.size(),
                dbTemplateDataList);

        return new Results(200, dbResults);

    }

    private DbTemplate getTemplateData(String templateUrl){

        ResponseEntity<DbTemplate> response = restTemplate.exchange(templateUrl,
                HttpMethod.GET, getHeaders(), DbTemplate.class);

        try{

            if (response.getStatusCode() == HttpStatus.OK) {
                // process the response
                DbTemplate dbTemplate = response.getBody();
                if (dbTemplate != null) {
                    return dbTemplate;
                }else {
                    return null;
                }
            }else {
                return null;
            }

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }



    }


    @Override
    public Results saveTemplates(DbTemplateData dbTemplateData) {

        Results results;

        String key = dbTemplateData.getVersionNumber();
        String program = dbTemplateData.getProgram();
        String description = dbTemplateData.getDescription();

        String templateUrl = internationalUrl + dataStore + master_template + "/" + key;

        // Get the metadata json
        ResponseEntity<JSONObject> metadataJson = restTemplate.exchange(internationalUrl+programsUrl,
                HttpMethod.GET, getHeaders(), JSONObject.class);

        if (metadataJson.getStatusCode() == HttpStatus.OK) {

            DbTemplate dbTemplate = new DbTemplate(
                    description,
                    program,
                    metadataJson);

            // Create the request headers
            HttpHeaders headers = new HttpHeaders();
            String auth = username+":"+password;
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            headers.add("Authorization", authHeader);

            // Create the request body as a Map
            HttpEntity<DbTemplate> request = new HttpEntity<>(dbTemplate, headers);

            ResponseEntity<DbSaveTemplate> response = restTemplate.postForEntity(
                    templateUrl, request, DbSaveTemplate.class);

            if (response.getStatusCodeValue() == 201){
                results = new Results(201, new DbDetails("The template has been saved successfully."));
            }else {
                results = new Results(400, new DbDetails("The resource cannot be saved"));
            }


        }else {
            results = new Results(400, new DbDetails("There was an issue in processing the request"));

        }

        return results;
    }


}
