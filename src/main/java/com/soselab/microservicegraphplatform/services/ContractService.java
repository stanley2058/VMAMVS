package com.soselab.microservicegraphplatform.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soselab.microservicegraphplatform.bean.mgp.MgpApplication;
import com.soselab.microservicegraphplatform.bean.neo4j.NullService;
import com.soselab.microservicegraphplatform.bean.neo4j.Service;
import com.soselab.microservicegraphplatform.bean.neo4j.ServiceRegistry;
import com.soselab.microservicegraphplatform.repositories.neo4j.GeneralRepository;
import com.soselab.microservicegraphplatform.repositories.neo4j.ServiceRegistryRepository;
import com.soselab.microservicegraphplatform.repositories.neo4j.ServiceRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Configuration
public class ContractService {

    private static final Logger logger = LoggerFactory.getLogger(SleuthService.class);

    @Autowired
    private GeneralRepository generalRepository;
    @Autowired
    private ServiceRegistryRepository serviceRegistryRepository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private SpringRestTool springRestTool;
    @Autowired
    private ObjectMapper mapper;


    public List<String> getAllServiceContractTestingCondition(String systemName){
        List<String> allServiceAppName = generalRepository.getSystemAllServiceName();
        List<String> temp = generalRepository.getAllServiceAndPathWithHTTP_REQUEST();


        List<Service> ServicesInDB = serviceRepository.findBySysName(systemName);

        for(Service s : ServicesInDB) {
            System.out.println(s.getAppName());
            System.out.println(generalRepository.getAllHttpRequestServiceWithService(s.getAppId()));

            List<String> providerService = generalRepository.getAllHttpRequestServiceWithService(s.getAppId());

            for(String str : providerService) {
                try {
                    JSONObject jsonObj = new JSONObject(str);
                    Map<String, Object> swaggerMap = springRestTool.getSwaggerFromRemoteApp2(jsonObj.getString("systemName"), jsonObj.getString("appName"), jsonObj.getString("version"));

                    if (swaggerMap != null) {
                        Map<String, Object> contractsMap = mapper.convertValue(swaggerMap.get("x-contract"), new TypeReference<Map<String, Object>>(){});
                        Map<String, Object> groovyMap = mapper.convertValue(contractsMap.get(s.getAppName().toLowerCase() + ".groovy"), new TypeReference<Map<String, Object>>(){});
                        groovyMap.forEach((key, value) -> {
                            Map<String, Object> apiMap = mapper.convertValue(value, new TypeReference<Map<String, Object>>(){});
                            Map<String, Object> testResultMap = mapper.convertValue(apiMap.get("testResult"), new TypeReference<Map<String, Object>>(){});
                            String status = mapper.convertValue(testResultMap.get("status"), new TypeReference<String>(){});

                            System.out.println("statusssssss: " + status);

                            if( status.equals("FAIL")) {
                                serviceRepository.setContractTestingConditionByAppId(s.getAppId(),"WARNING");
                            }



                        });



                    }


                }catch (JSONException err){
                    logger.error("Error", err.toString());
                }
            }



            //return generalRepository.getAllHttpRequestServiceWithService(s.getAppName());


            /*// 新增contractTestingCondition上去Service
            if (!serviceRepository.setContractTestingConditionByAppId(entry.getKey(), entry.getValue().getValue())){




            }*/

        }




/*        // For each service registry
        ArrayList<ServiceRegistry> registries = serviceRegistryRepository.findAll();
        if (registries.size() > 0) {
            for (ServiceRegistry serviceRegistry : registries) {
                // Get latest app list by request the first instance that own by this service registry
                if(serviceRegistry.getSystemName().toUpperCase().equals(systemName.toUpperCase())){
                    Map<String, Pair<MgpApplication, Integer>> eurekaAppsInfoAndNum =
                            springRestTool.getAppsInfoAndNumFromEurekaAppList(systemName, serviceRegistry.getAppId());
                    //List<Service> ServicesInDB = serviceRepository.findBySysName(serviceRegistry.getSystemName());
                    List<NullService> nullServiceInDB = serviceRepository.findNullBySysName(serviceRegistry.getSystemName());

                    for (Iterator<Map.Entry<String, Pair<MgpApplication, Integer>>> it = eurekaAppsInfoAndNum.entrySet().iterator(); it.hasNext();) {
                        Map.Entry<String, Pair<MgpApplication, Integer>> entry = it.next();
                        for (Service dbApp : ServicesInDB) {
                            if (entry.getKey().equals(dbApp.getAppId())) {
*//*                                isUpInDB = true;
                                break;*//*
                            }
                        }
                    }
                }

            }
        }*/

        return allServiceAppName;
    }


}
