package com.soselab.microservicegraphplatform.bean.mgp.notification.warning;

import com.soselab.microservicegraphplatform.bean.mgp.WebNotification;
import com.soselab.microservicegraphplatform.bean.mgp.notification.WarningNotification;
import com.soselab.microservicegraphplatform.botmq.MSABotSender;

public class SpcHighDurationRateNotification extends WarningNotification {
    private String appName;
    private String version;

    public SpcHighDurationRateNotification() {
    }

    public SpcHighDurationRateNotification(String appName, String version, Float value, Float threshold) {
        super("SPC high duration rate", createContent(appName, version, value, threshold), createHtmlContent(appName, version, value, threshold));
        this.appName = appName;
        this.version = version;
    }

    private static String createContent(String appName, String version, Float value, Float threshold) {
        String content = "Service \"" + appName + ":" + version +
                "\" exceeded the threshold of \"SPC high duration rate\": current value = " +
                value * 100 + "%, threshold = " + threshold * 100 + "%";
        MSABotSender msaBotSender = new MSABotSender();
        msaBotSender.send(content,WebNotification.LEVEL_WARNING);

        return content;
    }

    private static String createHtmlContent(String appName, String version, Float value, Float threshold) {
        return "Service <strong>" + appName + ":" + version +
                "</strong> exceeded the threshold of <strong>SPC high duration rate</strong>: current value = " +
                value * 100 + "%, threshold = " + threshold * 100 + "%";
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
