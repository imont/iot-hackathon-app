/**
 * Copyright 2017 IMONT Technologies
 * Created by romanas on 23/01/2017.
 */
package io.imont.hackathon;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

public class QNAPClient {

    private final BlockingConnection conn;

    public QNAPClient() throws Exception {
        MQTT mqtt = new MQTT();
        mqtt.setHost("192.168.55.13", 1883);
        mqtt.setClientId("CHIP");
        mqtt.setUserName("594a1c9a-d8f4-4710-834c-6f08277707d7");
        mqtt.setPassword("r:f5429dd18cc941e97e41a05a2af8f361");

        conn = mqtt.blockingConnection();
        conn.connect();
        System.out.println("MQTT Connected");
    }

    public void publish(String resourceName, String json) {
        try {
            conn.publish("qiot/things/admin/CHIP/" + resourceName, json.getBytes(), QoS.AT_LEAST_ONCE, false);
            System.out.println("Published " + resourceName + " :: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
