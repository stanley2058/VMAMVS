package com.soselab.microservicegraphplatform.bean.mgp.monitor.error;

import com.soselab.microservicegraphplatform.bean.mgp.monitor.ErrorEndpoint;
import com.soselab.microservicegraphplatform.bean.mgp.monitor.ErrorLink;
import com.soselab.microservicegraphplatform.bean.mgp.monitor.ErrorService;
import com.soselab.microservicegraphplatform.bean.mgp.monitor.MonitorError;

import java.util.ArrayList;

public class TestError extends MonitorError {
    ArrayList<ErrorService> errorServiceList = new ArrayList<>();
    ArrayList<ErrorEndpoint> errorEndpointList = new ArrayList<>();
    ArrayList<ErrorLink> errorLinkList = new ArrayList<>();

    public TestError() {
        super();

        setErrorAppId("CINEMA:GROCERYINVENTORY:0.0.1-SNAPSHOT");
        setErrorSystemName("CINEMA");
        setErrorAppName("groceryinventory");
        setErrorAppVersion("0.0.1-SNAPSHOT");
        setConsumerAppName("groceryinventory");
        setStatusCode("500");
        setErrorMessage("Request processing failed; nested exception is java.lang.NumberFormatException: For input string: \"\"");
        setErrorPath("/getGroceryByID");
        setErrorUrl("http://http://140.121.197.130:4153/getGroceryByID?ID=");
        setErrorMethod("GET");
        setErrorType("RequestError");
        setTestedPASS(true);
        errorServiceList.add(new ErrorService(888, "GROCERYINVENTORY", "0.0.1-SNAPSHOT", "CINEMA:GROCERYINVENTORY:0.0.1-SNAPSHOT", true));
        setErrorServices(errorServiceList);

        errorEndpointList.add(new ErrorEndpoint(889, "CINEMA:GROCERYINVENTORY:0.0.1-SNAPSHOT", "GROCERYINVENTORY", "/getGroceryByID", true));
        setErrorEndpoints(errorEndpointList);

        errorLinkList.add(new ErrorLink(890, 888, "OWN", 889, true));
        setErrorLinks(errorLinkList);
    }
}
