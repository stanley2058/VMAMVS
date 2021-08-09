package com.soselab.microservicegraphplatform.services;

import com.soselab.microservicegraphplatform.bean.mgp.monitor.MonitorError;
import com.soselab.microservicegraphplatform.bean.mgp.monitor.error.*;
import com.soselab.microservicegraphplatform.bean.neo4j.Service;
import com.soselab.microservicegraphplatform.repositories.neo4j.ServiceRepository;
//import jdk.vm.ci.services.Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class MonitorErrorSimulator {

    private final int totalDay = 210; // 總天數140+28天，當天到前28天計算各服務錯誤總數量，前29~168天計算各服務風險值
    private final int hours = 24; // 一天24小時

    @Autowired
    private ServiceRepository serviceRepository;

    Random random = new Random(0);
    final int minErrorNum = 500;
    final int maxErrorNum = 1000;

    final int timeInterval = 140;

    private static final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final HashMap<String, List<MonitorError>> systemErrorMap = new HashMap<>();

    public MonitorErrorSimulator(){}

    public void clear() {
        systemErrorMap.clear();
    }


    // 視覺化 https://noob.tw/web-visualization-chartjs/
    // https://www.ucamc.com/e-learning/javascript/270-%E7%B0%A1%E5%96%AE%E4%BD%BF%E7%94%A8chart-js%E7%B6%B2%E9%A0%81%E4%B8%8A%E7%95%AB%E5%9C%96%E8%A1%A8%E7%AF%84%E4%BE%8B%E9%9B%86-javascript-%E5%9C%96%E8%A1%A8%E3%80%81jquery%E5%9C%96%E8%A1%A8%E7%B9%AA%E8%A3%BD
    private List<MonitorError> simulate(String systemName) {
        if (systemErrorMap.containsKey(systemName)) return systemErrorMap.get(systemName);

        List<Service> services = serviceRepository.findBySysName(systemName);
        for(Service s : services){
            if(s.getAppName().equalsIgnoreCase("CINEMACATALOG") && s.getVersion().equals("0.0.1-SNAPSHOT")){
//                s.setErrorProbability(0.02);
                s.setErrorProbability(0.006);
            }else if(s.getAppName().equalsIgnoreCase("GROCERYINVENTORY") && s.getVersion().equals("0.0.1-SNAPSHOT")){
//                s.setErrorProbability(0.00);
                s.setErrorProbability(0.007);
            }else if(s.getAppName().equalsIgnoreCase("ORDERING") && s.getVersion().equals("0.0.1-SNAPSHOT")){
//                s.setErrorProbability(0.09);
                s.setErrorProbability(0.010);
            }else if(s.getAppName().equalsIgnoreCase("PAYMENT") && s.getVersion().equals("0.0.1-SNAPSHOT")){
//                s.setErrorProbability(0.03);
                s.setErrorProbability(0.003);
            }else if(s.getAppName().equalsIgnoreCase("NOTIFICATION") && s.getVersion().equals("0.0.1-SNAPSHOT")){
//                s.setErrorProbability(0.00);
                s.setErrorProbability(0.004);
            }
        }

        List<MonitorError> monitorErrors = new ArrayList<>();
        long nowTime = System.currentTimeMillis();

        HashMap<String, Integer> requestCount = new HashMap<>();
        HashMap<String, Integer> errorCount = new HashMap<>();

        for(int i = 0; i < totalDay; i++) {
            for(Service s : services){
                int randomErrorNum = random.nextInt((maxErrorNum - minErrorNum) + 1) + minErrorNum;
                requestCount.putIfAbsent(s.getAppName(), 0);
                errorCount.putIfAbsent(s.getAppName(), 0);
                requestCount.merge(s.getAppName(), randomErrorNum, Integer::sum);
                for(int j = 0; j < randomErrorNum; j++) {
                    if(rateRandom(s.getErrorProbability())){
                        errorCount.merge(s.getAppName(), 1, Integer::sum);

                        MonitorError monitorError = null;
                        long errorTimestamp = nowTime - i * 24 * 60 * 60 * 1000L - (random.nextInt(24) + 1) * 60 * 60 * 1000L;
                        if(s.getAppName().equalsIgnoreCase("CINEMACATALOG") && s.getVersion().equals("0.0.1-SNAPSHOT")){
                            monitorError = new RequestError();
                        }else if(s.getAppName().equalsIgnoreCase("GROCERYINVENTORY") && s.getVersion().equals("0.0.1-SNAPSHOT")){
                            monitorError = new TestError();
                        }else if(s.getAppName().equalsIgnoreCase("ORDERING") && s.getVersion().equals("0.0.1-SNAPSHOT")){
                            monitorError = new ResponseError();
                        }else if(s.getAppName().equalsIgnoreCase("PAYMENT") && s.getVersion().equals("0.0.1-SNAPSHOT")){
                            monitorError = new NodeError();
                        }else if(s.getAppName().equalsIgnoreCase("NOTIFICATION") && s.getVersion().equals("0.0.1-SNAPSHOT")){
                            monitorError = new TestError2();
                        }

                        if (monitorError != null) {
                            monitorError.setTimestamp(errorTimestamp * 1000L);
                            monitorError.setDate(dateFormat2.format(errorTimestamp));
                            monitorErrors.add(monitorError);
                        }
                    }
                }
            }
        }

        System.out.println(requestCount);
        System.out.println(errorCount);

        systemErrorMap.put(systemName, monitorErrors);
        return monitorErrors;
    }

    public  List<MonitorError> simulateErrors(String systemName){
        return simulate(systemName);
    }

    public int getSimulatedErrorAmountInRange(String systemName, String appName, String appVersion, long lookBack, long endTime, int limit) {
        final HashSet<String> errorSet = new HashSet<>(Arrays.asList("500", "502", "503", "504"));
        final long start = endTime - lookBack;
        return simulate(systemName).stream().filter(e ->
                e.getTimestamp() / 1000 >= start &&
                e.getTimestamp() / 1000 < endTime &&
                e.getErrorAppName().equalsIgnoreCase(appName) &&
                e.getErrorAppVersion().equalsIgnoreCase(appVersion) &&
                errorSet.contains(e.getStatusCode())
        ).limit(limit).toArray().length;
    }


    public boolean rateRandom(double rate) {
        final int num = random.nextInt(10000) + 1;
        return num <= (int) (rate * 1000);
    }
}
