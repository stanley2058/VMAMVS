package com.soselab.microservicegraphplatform.bean.mgp.monitor;


import java.util.ArrayList;

public class MonitorError {

    // timestamp使用 https://codertw.com/%E7%A8%8B%E5%BC%8F%E8%AA%9E%E8%A8%80/319101/
    private String errorAppName;
    private String errorAppVersion;
    private String consumerAppName;
    private long timestamp;
    private String statusCode;
    private String errorMessage;
    private String errorPath;

    private ArrayList<ErrorService> errorServices;
    private ArrayList<ErrorEndpoint> errorEndpoints;
    private ArrayList<ErrorLink> errorLinks;

    public String getErrorAppName() {
        return errorAppName;
    }

    public void setErrorAppName(String errorAppName) {
        this.errorAppName = errorAppName;
    }

    public String getErrorAppVersion() {
        return errorAppVersion;
    }

    public void setErrorAppVersion(String errorAppVersion) {
        this.errorAppVersion = errorAppVersion;
    }

    public String getConsumerAppName() {
        return consumerAppName;
    }

    public void setConsumerAppName(String consumerAppName) {
        this.consumerAppName = consumerAppName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ArrayList<ErrorService> getErrorServices() {
        return errorServices;
    }

    public void setErrorServices(ArrayList<ErrorService> errorServices) {
        this.errorServices = errorServices;
    }

    public ArrayList<ErrorEndpoint> getErrorEndpoints() {
        return errorEndpoints;
    }

    public void setErrorEndpoints(ArrayList<ErrorEndpoint> errorEndpoints) {
        this.errorEndpoints = errorEndpoints;
    }

    public ArrayList<ErrorLink> getErrorLinks() {
        return errorLinks;
    }

    public void setErrorLinks(ArrayList<ErrorLink> errorLinks) {
        this.errorLinks = errorLinks;
    }

    public String getErrorPath() {
        return errorPath;
    }

    public void setErrorPath(String errorPath) {
        this.errorPath = errorPath;
    }
}