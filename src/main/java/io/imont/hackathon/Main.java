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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Main {

    private static final Map<String, String> qnapIdMap = new HashMap<>();

    static {
        qnapIdMap.put("000D6F0004B63317", "CONTACT");
        qnapIdMap.put("000D6F0004B645C9", "MOTION");
    }

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static QNAPClient qnapClient;

    public static void main(String[] args) throws Exception {
        qnapClient = new QNAPClient();

        FerretConfiguration fc = new FerretConfiguration();
        fc.setFriendlyName("Hackathon");

        Lion lion = new LionBuilder().ferretConfiguration(fc).workDir(".").build();

        Properties props = new Properties();
        //props.put("telegesis.serial.device", "/dev/tty.SLAB_USBtoUART");
        props.put("telegesis.serial.device", "/dev/ttyUSB0");
        props.put("telegesis.serial.baud", "115200");
        HornetProperties hp = new HornetProperties(props);

        lion.getDriverManager().registerBundle(new BasicBundle());
        lion.registerNetwork("ZigBee", new ZigBeeNetwork(hp));

        lion.start();

        printDevices(lion.getMole());
        subscribeToEvents(lion.getMole());

        //String localPeerId = lion.getMole().getLocalPeerId();

        //lion.discover(localPeerId).subscribe(discoveredDevice -> System.out.println("Found device: " + discoveredDevice));
    }

    private static void printDevices(MoleClient mole) throws Exception {
        for (String s : mole.getAllEntityIds()) {
            System.out.println("Device: " + s);
            System.out.println("State: " + mole.getState(s));
        }
    }

    private static void subscribeToEvents(MoleClient mole) {
        mole.subscribeToEvents(event -> {
            String qnapName = qnapIdMap.get(event.getEntityId());
            if (qnapName != null) {
                logger.info("Got event from {}, key={}, value={}", event.getEntityId(), event.getKey(), event.getValue());
                if (event.getKey().contains("MOTION")) {
                    long time = System.currentTimeMillis() / 1000;
                    qnapClient.publish(String.format("%s_DETECTED", qnapName),
                            String.format("{\"value\": %d}", time));
                } else if (event.getKey().contains("TEMPERATURE")) {
                    qnapClient.publish(String.format("%s_TEMPERATURE", qnapName),
                            String.format("{\"value\": %f}", Float.parseFloat(event.getValue())));
                } else if (event.getKey().contains("OPEN_CLOSED")) {
                    boolean status = event.getValue().equals("OPEN");
                    qnapClient.publish(String.format("%s_STATUS", qnapName),
                            String.format("{\"value\": %b}", status));
                }
            }

        });
    }

}
