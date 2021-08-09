package com.soselab.microservicegraphplatform.bean.mgp.monitor.error;

import com.soselab.microservicegraphplatform.bean.mgp.monitor.ErrorEndpoint;
import com.soselab.microservicegraphplatform.bean.mgp.monitor.ErrorLink;
import com.soselab.microservicegraphplatform.bean.mgp.monitor.ErrorService;
import com.soselab.microservicegraphplatform.bean.mgp.monitor.MonitorError;

import java.util.ArrayList;

public class TestError2 extends MonitorError {
    ArrayList<ErrorService> errorServiceList = new ArrayList<>();
    ArrayList<ErrorEndpoint> errorEndpointList = new ArrayList<>();
    ArrayList<ErrorLink> errorLinkList = new ArrayList<>();

    public TestError2() {
        super();

        setErrorAppId("CINEMA:NOTIFICATION:0.0.1-SNAPSHOT");
        setErrorSystemName("CINEMA");
        setErrorAppName("notification");
        setErrorAppVersion("0.0.1-SNAPSHOT");
        setConsumerAppName("notification");
        setStatusCode("500");
        setErrorMessage("Request processing failed; nested exception is java.lang.NumberFormatException: For input string: \"\"");
        setErrorPath("/deleteNotification");
        setErrorUrl("http://http://140.121.197.130:4153/deleteNotification");
        setErrorMethod("GET");
        setErrorType("RequestError");
        setTestedPASS(true);
        errorServiceList.add(new ErrorService(988, "NOTIFICATION", "0.0.1-SNAPSHOT", "CINEMA:NOTIFICATION:0.0.1-SNAPSHOT", true));
        setErrorServices(errorServiceList);

        errorEndpointList.add(new ErrorEndpoint(989, "CINEMA:NOTIFICATION:0.0.1-SNAPSHOT", "NOTIFICATION", "/deleteNotification", true));
        setErrorEndpoints(errorEndpointList);

        errorLinkList.add(new ErrorLink(990, 988, "OWN", 989, true));
        setErrorLinks(errorLinkList);
    }
}
