package com.soselab.microservicegraphplatform.services;

import com.soselab.microservicegraphplatform.bean.mgp.monitor.MonitorError;
import com.soselab.microservicegraphplatform.bean.mgp.monitor.chart.RiskPositivelyCorrelatedChart;
import com.soselab.microservicegraphplatform.bean.neo4j.Service;
import com.soselab.microservicegraphplatform.repositories.neo4j.GeneralRepository;
import com.soselab.microservicegraphplatform.repositories.neo4j.ServiceRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

@Configuration
public class RiskService {

    private static final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Autowired
    private SleuthService sleuthService;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private GeneralRepository generalRepository;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private MonitorErrorSimulator monitorErrorSimulator;

    private final int totalDay = 84; // 總天數84天
    private final int timeInterval = 28; // 28天為間隔
    private final int moveInterval = 1; // 每次移動的距離
    private final int windowWidth = 28; // 28天為間隔

    private final int beginTime1 = 43; // 第2周開始 -> 2
    private final int endTime1 = 70; // 到第4周 -> 5
    private final int beginTime2 = 71; // 第5周開始 -> 6
    private final int endTime2 = 147; // 到第12周 -> 17

//    private final int STATUSCODE500 = 500;
//    private final int STATUSCODE502 = 502;
//    private final int STATUSCODE503 = 503;
//    private final int STATUSCODE504 = 504;

    Map<String,Double> thisWeekErrorNumMap = new HashMap<>();

    private ArrayList<Integer> getHighAndLowStandardErrors(List<Service> services, int limit) {
        ArrayList<Integer> highAndLowStandardErrors = new ArrayList<>();
        for(Service s : services) {
            long startMillis = System.currentTimeMillis() - endTime2 * 86400000L;
            final long endMillis = System.currentTimeMillis() - beginTime2 * 86400000L;
            final long windowMillis = windowWidth * 86400000L;

            while (endMillis - startMillis >= windowMillis) {
                // real
//                int serviceErrors = sleuthService.searchZipkinForErrorAmountV1(s.getAppName(), s.getVersion(), lookBack, endTime, limit);
                // testing
                final int serviceErrors = monitorErrorSimulator.getSimulatedErrorAmountInRange(
                        s.getSystemName(),
                        s.getAppName(),
                        s.getVersion(),
                        startMillis,
                        startMillis + windowMillis,
                        limit
                );
                highAndLowStandardErrors.add(serviceErrors);
                startMillis += 86400000L;
            }
        }
        Collections.sort(highAndLowStandardErrors);

        System.out.println("Reference Error Count:");
        HashMap<String, Integer> totalErrors = new HashMap<>();
        for(Service s : services) {
            totalErrors.put(
                    s.getAppId(),
                    monitorErrorSimulator.getSimulatedErrorAmountInRange(
                            s.getSystemName(),
                            s.getAppName(),
                            s.getVersion(),
                            System.currentTimeMillis() - endTime2 * 86400000L,
                            System.currentTimeMillis() - beginTime2 * 86400000L,
                            limit
                    )
            );
        }
        System.out.println(totalErrors);

        return highAndLowStandardErrors;
    }
    private double getHighStandard(ArrayList<Integer> highAndLowStandardErrors) {
        int highStandardTotal = 0;
        for(int i = highAndLowStandardErrors.size() - 1; i > highAndLowStandardErrors.size() * 0.75 + 1; i--)
            highStandardTotal += highAndLowStandardErrors.get(i);
        final int highStandardCount = (int)((double)highAndLowStandardErrors.size() / 4 + 1);
        return (double)highStandardTotal / highStandardCount;
    }
    private double getLowStandard(ArrayList<Integer> highAndLowStandardErrors) {
        int lowStandardTotal = 0;
        for(int i = 0; i < highAndLowStandardErrors.size() / 4 - 1; i++)
            lowStandardTotal += highAndLowStandardErrors.get(i);
        final int lowStandardCount = (int)((double)highAndLowStandardErrors.size() / 4 - 1);
        return (double)lowStandardTotal / lowStandardCount;
    }

    private Map<String, Double> getServiceErrorCountMap(List<Service> services, int limit) {
        Map<String, Double> serviceErrorCountMap = new HashMap<>();
        for(Service s : services) {
            long startMillis = System.currentTimeMillis() - endTime1 * 86400000L;
            final long endMillis = System.currentTimeMillis() - beginTime1 * 86400000L;
//                double serviceErrors = sleuthService.searchZipkinForErrorAmountV1(s.getAppName(), s.getVersion(), lookBack, endTime, limit);
            final double serviceErrors = monitorErrorSimulator.getSimulatedErrorAmountInRange(
                    s.getSystemName(),
                    s.getAppName(),
                    s.getVersion(),
                    startMillis,
                    endMillis,
                    limit
            );
            serviceErrorCountMap.put(s.getAppId(), serviceErrors);
        }
        System.out.println("Analysis Error Count:");
        System.out.println(serviceErrorCountMap);
        return serviceErrorCountMap;
    }

    private Map<String, Double> getEndpointCountMap(List<Service> services) {
        Map<String,Double> endpointCountMap = new HashMap<>();
        for(Service s : services) {
            JSONArray providerNodes = new JSONObject(generalRepository.getProviders(s.getId())).getJSONArray("nodes");
            JSONArray consumerNodes = new JSONObject(generalRepository.getConsumers(s.getId())).getJSONArray("nodes");

            double totalNum = 0;
            for(int j = 0; j < providerNodes.length(); j++)
                totalNum += getNumOfEndpointProvider((int)providerNodes.getJSONObject(j).get("id"));
            for(int j = 0; j < consumerNodes.length(); j++)
                totalNum += getNumOfEndpointConsumer((int)consumerNodes.getJSONObject(j).get("id"));

            endpointCountMap.put(s.getAppId(), (totalNum + 1));
        }
        System.out.println("Endpoint Count Map:");
        System.out.println(endpointCountMap);
        return endpointCountMap;
    }

    private Map<String, Double> getCurrentWeekErrorCountMap(List<Service> services, Map<String, Double> endpointCountMap, long endTime, long lookBack, int limit) {
        Map<String, Double> currentWeekErrorMap = new HashMap<>();
        Map<String, Integer> errorMap = new HashMap<>();
        for(Service s : services) {
            // real
//                int errorAmount = sleuthService.searchZipkinForErrorAmountV1(s.getAppName(), s.getVersion(), lookBack, endTime, limit);
            // testing

            final int errorAmount = monitorErrorSimulator.getSimulatedErrorAmountInRange(s.getSystemName(), s.getAppName(), s.getVersion(), lookBack, endTime, limit);
//            final double serviceErrors = errorAmount * endpointCountMap.get(s.getAppId()) + errorAmount;
            final double serviceErrors = errorAmount * endpointCountMap.get(s.getAppId());
            errorMap.put(s.getAppId(), errorAmount);
            currentWeekErrorMap.put(s.getAppId(), serviceErrors);
        }
        System.out.println("Current Week:");
        System.out.println(errorMap);
        System.out.println(currentWeekErrorMap);
        return currentWeekErrorMap;
    }

    public void setServiceRisk(String systemName) {
        final long nowTime = System.currentTimeMillis();
        final long lookBack = timeInterval * 24 * 60 * 60 * 1000L; // 實際使用天數為單位
//        final long lookBack = timeInterval * 60 * 1000L; // 模擬錯誤用分鐘為單位

        // 用來計算第一周的衍生錯誤
        final long lookBackThisWeek = 28 * 24 * 60 * 60 * 1000L; // 實際使用天數為單位
//        final long lookBackThisWeek = 7 * 60 * 1000L; // 模擬錯誤用分鐘為單位

        final long move = moveInterval * 24 * 60 * 60 * 1000L; // 實際使用天數為單位
//        final long move = moveInterval * 60 * 1000L; // 模擬用分鐘為單位
        final int limit = 10000;

        final List<Service> services = serviceRepository.findBySysName(systemName);

        // Likelihood
        // 第5周~12周(8周) ==> 算高標(ex:8.5)、低標(ex:1.1)
        // 第2周~第4周(3周) ==> 找各服務所有的錯誤數，算風險值 (根據高低標縮放比例，縮放至1~0.1)

        // reference
        // 第5周~12周(8周) ==> 算高標(ex:8.5)、低標(ex:1.1)
        final long highAndLowEndTime = nowTime - beginTime2 * 24 * 60 * 60 * 1000L; // 實際用天數為單位
//        final long highAndLowEndTime = nowTime - beginTime2 * 60 * 1000L; // 模擬用分鐘數為單位
        final ArrayList<Integer> highAndLowStandardErrors = getHighAndLowStandardErrors(
                services,
                limit
        );
        final double highStandard = getHighStandard(highAndLowStandardErrors);
        final double lowStandard = getLowStandard(highAndLowStandardErrors);

        // analysis
        // 第2周~第4周(3周) ==> 找各服務所有的錯誤數，算風險值 (根據高低標縮放比例，縮放至1~0.1)
        final long endTime = nowTime - beginTime1 * 24 * 60 * 60 * 1000L; // 實際用天數為單位
//        final long endTime = nowTime - beginTime1 * 60 * 1000L; // 模擬用分鐘為單位
        final Map<String, Double> serviceErrorLikelihoodMap = normalizationLikelihood(getServiceErrorCountMap(
                services,
                limit
        ), highStandard, lowStandard); // 正規化[0.1, 1]

        // 找出服務影響到的端點數量 & 正規化[0.1, 1]
//        final Map<String, Double> endpointImpactMap = normalizationImpact(getEndpointCountMap(services), services);

        // verify
        // 先寫死，要發paper啦
        final Map<String, Double> endpointImpactMap = new HashMap<>();
        endpointImpactMap.put("CINEMA:GROCERYINVENTORY:0.0.1-SNAPSHOT", 1.0);
        endpointImpactMap.put("CINEMA:ORDERING:0.0.1-SNAPSHOT", 2.0);
        endpointImpactMap.put("CINEMA:PAYMENT:0.0.1-SNAPSHOT", 2.0);
        endpointImpactMap.put("CINEMA:CINEMACATALOG:0.0.1-SNAPSHOT", 1.0);
        endpointImpactMap.put("CINEMA:NOTIFICATION:0.0.1-SNAPSHOT", 2.0);
        final Map<String, Double> endpointImpactNormalMap = normalizationImpact(endpointImpactMap, services);

        // 計算RiskValue，放到neo4j存
        for(Service s : services) {
            double riskValue = serviceErrorLikelihoodMap.get(s.getAppId()) * endpointImpactNormalMap.get(s.getAppId());
            serviceRepository.setRiskValueByAppId(s.getAppId(), riskValue);
        }

        // 第1周 ==> 找各服務所有的錯誤數，算衍生錯誤
        thisWeekErrorNumMap.putAll(getCurrentWeekErrorCountMap(
                services,
                endpointImpactMap,
                nowTime,
                lookBackThisWeek,
                limit
        ));
    }

    public RiskPositivelyCorrelatedChart getRiskPositivelyCorrelatedChart(String systemName){
        RiskPositivelyCorrelatedChart riskPositivelyCorrelatedChart = new RiskPositivelyCorrelatedChart();

        List<Service> services = serviceRepository.findBySysName(systemName);
        Map<String, Double> risk = new HashMap<>();
        for(Service s : services) risk.put(s.getAppId(), serviceRepository.getRiskValueByAppId(s.getAppId()));

        riskPositivelyCorrelatedChart.setServicesErrorNum(thisWeekErrorNumMap);
        riskPositivelyCorrelatedChart.setRisk(risk);
        return riskPositivelyCorrelatedChart;
    }

    private double getNumOfEndpoint(long id, Function<Long, String> getNext) {
        JSONArray nodes = new JSONObject(getNext.apply(id)).getJSONArray("nodes");

        if (nodes.length() <= 0) return 0;
        double totalNum = nodes.length();
        for(int j = 0; j < nodes.length(); j++) {
            totalNum += getNumOfEndpoint((int)nodes.getJSONObject(j).get("id"), getNext);
        }
        return totalNum;
    }
    public double getNumOfEndpointProvider(long id) {
        return getNumOfEndpoint(id, generalRepository::getConsumers);
    }
    public double getNumOfEndpointConsumer(long id) {
        return getNumOfEndpoint(id, generalRepository::getProviders);
    }

    // 正規化[0.1, 1]
    public Map<String,Double> normalizationLikelihood(Map<String,Double> map, double highStandard, double lowStandard){
        double a = 0.1;
        double b = 1;

        Map<String,Double> returnMap = new HashMap<>();

        System.out.println("\nnormalization_likelihood: ");
        if (map != null) {
            double max = highStandard;
            double min = lowStandard;
            double k = 0.0;

            // 計算係數k
            k = (b - a) / (max - min);
            System.out.println("max:" + max);
            System.out.println("min:" + min);
            System.out.println("k:" + k);

            // 套入公式正規化
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                String key = entry.getKey();
                double value = entry.getValue();

                double NorY = a + k * (value - min);

                if(NorY > 1.0) NorY = 1.0;
                if(NorY < 0.1) NorY = 0.1;

                returnMap.put(key, NorY);

                System.out.println(key + ": " + value + " --> " + NorY);
            }
        }

        return returnMap;
    }


    // 正規化[0.1, 1]
    public Map<String,Double> normalizationImpact(Map<String,Double> map, List<Service> ServicesInDB){
        double a = 0.1;
        double b = 1;

        Map<String,Double> returnMap = new HashMap<>();

        System.out.println("\nnormalization_impact: ");
        if (map != null) {
            double max = map.get(ServicesInDB.get(0).getAppId());
            double min = map.get(ServicesInDB.get(0).getAppId());
            double k;

            // 找max , min
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                String key = entry.getKey();
                double value = entry.getValue();
                if(max < value)
                    max = value;
                if(min > value)
                    min = value;
            }

            // 計算係數k
            k = (b-a)/(max-min);
            System.out.println("max:"  + max);
            System.out.println("min:"  + min);
            System.out.println("k:"  + k);

            // 套入公式正規化
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                double NorY = a + k * ((double)value - min);

                returnMap.put(key, NorY);

                System.out.println(key + ": " + value + " --> " + NorY);
            }
        }

        return returnMap;
    }

}
