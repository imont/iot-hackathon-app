/**
 * Copyright 2017 IMONT Technologies
 * Created by romanas on 23/01/2017.
 */
package io.imont.hackathon;

import io.imont.ferret.client.config.FerretConfiguration;
import io.imont.hornet.basic.drivers.BasicBundle;
import io.imont.hornet.configuration.HornetProperties;
import io.imont.hornet.lion.ZigBeeNetwork;
import io.imont.lion.Lion;
import io.imont.lion.LionBuilder;
import io.imont.mole.MoleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
//        MQTT mqtt = new MQTT();
//        mqtt.setHost("192.168.55.13", 1883);
//        mqtt.setClientId("CHIP");
//        mqtt.setUserName("594a1c9a-d8f4-4710-834c-6f08277707d7");
//        mqtt.setPassword("r:f5429dd18cc941e97e41a05a2af8f361");
//
//        BlockingConnection conn = mqtt.blockingConnection();
//        conn.connect();
//        System.out.println("Connected");
//
//        Random rnd = new Random();

//        while (true) {
//            int temp = rnd.nextInt(100);
//
//            String telemetry = String.format("{\"value\":%d}", temp);
//            String motionTelemetry = String.format("{\"value\":%b}", temp % 2 == 0);
//            conn.publish("qiot/things/admin/CHIP/temp_room1", telemetry.getBytes(), QoS.AT_LEAST_ONCE, false);
//            conn.publish("qiot/things/admin/CHIP/motion_room1", motionTelemetry.getBytes(), QoS.AT_LEAST_ONCE, false);
//            System.out.println("SENT " + telemetry);
//            System.out.println("SENT " + motionTelemetry);
//            Thread.sleep(2000);
//        }

        FerretConfiguration fc = new FerretConfiguration();
        fc.setFriendlyName("Hackathon");

        Lion lion = new LionBuilder().ferretConfiguration(fc).workDir(".").build();

        Properties props = new Properties();
        props.put("telegesis.serial.device", "/dev/tty.SLAB_USBtoUART");
        props.put("telegesis.serial.baud", "115200");
        HornetProperties hp = new HornetProperties(props);

        lion.getDriverManager().registerBundle(new BasicBundle());
        lion.registerNetwork("ZigBee", new ZigBeeNetwork(hp));

        lion.start();

        printDevices(lion.getMole());
        subscribeToEvents(lion.getMole());

        String localPeerId = lion.getMole().getLocalPeerId();

        lion.discover(localPeerId).subscribe(discoveredDevice -> System.out.println("Found device: " + discoveredDevice));
    }

    private static void printDevices(MoleClient mole) throws Exception {
        for (String s : mole.getAllEntityIds()) {
            System.out.println("Device: " + s);
            System.out.println("State: " + mole.getState(s));
        }
    }

    private static void subscribeToEvents(MoleClient mole) {
        mole.subscribeToEvents(event -> {
            logger.info("Got event from {}, key={}, value={}", event.getEntityId(), event.getKey(), event.getValue());
        });
    }

}
