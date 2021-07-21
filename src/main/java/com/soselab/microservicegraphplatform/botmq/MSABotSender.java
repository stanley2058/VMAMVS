package com.soselab.microservicegraphplatform.botmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONObject;

import java.net.ConnectException;

/*#####################################################
###	2019/12/09										###
###	created by jimting								###
###	for MSABot project, and my graduate paper QAQ	###
#######################################################*/


public class MSABotSender {

    private static final String EXCHANGE_NAME = "vmamv";


    private static final String mqip = "140.121.197.130";
    private static final int mqport = 5502;
    private static final String roomID = "C9PF9PKTL";

    public boolean send(String content, String status) {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(mqip);
        factory.setPort(mqport);

        JSONObject obj = new JSONObject();
        obj.put("roomID", roomID);
        obj.put("content", content);
        obj.put("status", status);

        String result = obj.toString();

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            channel.basicPublish(EXCHANGE_NAME, "", null, result.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + result + "'");
            return true;
        } catch (ConnectException e1) {
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return false;
    }

}


