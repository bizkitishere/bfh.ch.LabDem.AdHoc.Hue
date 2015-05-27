/*
 *  BfhChLabDemAdHocHue.java
 *  LabDem
 *
 *  Copyright (c) 2015 BFH. All rights reserved.
 */
package bfh.ch.labdem.adhoc.hue.main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.Timer;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 * @author Philippe Lüthi, Elia Kocher
 */
public class BfhChLabDemAdHocHue {

    //MQTT broker information
    private final static String PROTOCOL = "tcp";
    private final static String BROKER = "localhost";
    private final static String PORT = "1883";
    private final static String TOPIC_MAIN = "LabDem";
    private final static String TOPIC_SERVER2HW = "/Server2HW";
    private final static String TOPIC_HW2SERVER = "/HW2Server";
    private final static String WILL = MQTTMessages.OfflineAdHocHue.toString();

    //HTTP POST request
    private static URL url;
    private final static String TARGET_NAME = "targetName";
    private final static String COMMAND = "command";
    private final static String VALUE= "value";
    private final static String SERVER_URL = "http://vmnashira.bfh.ch:8080/HueLampApi/HueLampsApi";
        
    private static Subscriber s;
    private static Publisher p;
    
    //contains ids to "group" actions that belong together,
    //thus the error message will be sent only once
    private static final ArrayList<Integer> MESSAGEIDS = new ArrayList<>();
    
    private static final Timer TIMER = new Timer();
    
    //HUE lamps have type 1
    public final static int HARDWARE_TYPE_ID = 1;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try {
            //subscriber setup
            s = new Subscriber(PROTOCOL, BROKER, PORT, TOPIC_MAIN + TOPIC_SERVER2HW, WILL, ClientType.Subscriber);
            s.connectToBroker();
            s.subscribe();
            
            //publisher setup
            p = new Publisher(PROTOCOL, BROKER, PORT, TOPIC_MAIN + TOPIC_HW2SERVER, WILL, ClientType.Publisher);
            p.connectToBroker();
            p.Publish(MQTTMessages.Online.toString(), 1, true);

            //http post request setup
            url = new URL(SERVER_URL);
            
            //prepare the cleaner to empty the list of ids once every day
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 03);
            cal.set(Calendar.MINUTE, 0);
            Cleaner c = new Cleaner(MESSAGEIDS);
            //executed the run of Cleaner once every 24 hours, starting at 3 in the morning
            TIMER.schedule(c, cal.getTime(), 86400000);
            
            System.out.println(cal.getTime());
            
        } catch (MqttException | MalformedURLException ex) {
            //if mqtt does not work, shut down, the "will" will notify the daemon
            System.exit(1);
        } 
        
    }
 
    /**
     * Sends a message to the Hue Lamp Service using a HTTP GET request
     * @param name Target name
     * @param command command
     * @param value value (of the command)
     * @param messageId used to group actions that belong together
     */
    public static void sendToHardware(String name, String command, String value, int messageId){
        
        try {
            // opens the connection to the server
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            
            // Indicate that we want to write to the HTTP request body
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            
            // Writing the post data to the HTTP request body
            try (BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()))) {
                httpRequestBodyWriter.write(TARGET_NAME + "=" + name + "&" + COMMAND + "=" + command + "&" + VALUE + "=" + value);
            } 
            // Reading from the HTTP response body
            try (Scanner httpResponseScanner = new Scanner(urlConnection.getInputStream())) {
                while(httpResponseScanner.hasNextLine()) {
                    System.out.println(httpResponseScanner.nextLine());
                }
            }

        } catch (IOException ex) {
            //send error message to server, only if a message of the same "group" has not yet been sent
            if(!MESSAGEIDS.contains(messageId)){
                sendToServer(MQTTMessages.LampServletOffline.toString());
                MESSAGEIDS.add(messageId);
            }
            
        }
    }
    
    /**
     * sends a message to the Daemon Server
     * used to send error messages only, therefore this message will be sent
     * once only until a message could be send successful
     * will terminate if the message could not be sent
     * @param m message to send
     */
    public static void sendToServer(String m) {
        try {
            p.Publish(m, 1, true);
        } catch (MqttException ex) {
            //no possibility to contact daemon, need to shut down this programme -> causes message "offline" on topic
            System.exit(1);
        }
    }
    
    /**
     * enum containing the different client types
     */
    public enum ClientType{
        Subscriber,
        Publisher
    }
    
    /**
     * enum containing different messages that will be sent using MQTT
     */
    public enum MQTTMessages{
        Online,
        Offline,
        OfflineAdHocHue,
        LampServletOffline
    }
    
}
