/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bfh.ch.labdem.adhoc.hue.main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 *
 * @author Philippe Lüthi, Elia Kocher
 */
public class BfhChLabDemAdHocHue {

    //MQTT broker information
    private final static String PROTOCOL = "tcp";
    //private final static String BROKER = "broker.mqttdashboard.com"; //public broker, for test purposes
    //private final static String BROKER = "147.87.117.73"; //LabDem broker
    private final static String BROKER = "localhost";
    private final static String PORT = "1883";
    private final static String TOPIC_MAIN = "LabDem";
    private final static String TOPIC_SERVER2HW = "/Server2HW";
    private final static String TOPIC_HW2SERVER = "/HW2Server";
    private final static String WILL = MQTTMessages.Offline.toString();

    //HTTP POST request
    private static URL url;
    private final static String TARGET_NAME = "targetName";
    private final static String COMMAND = "command";
    private final static String VALUE= "value";
    //private final static String SERVER_URL = "http://" + BROKER + "/test2/index.php";
    private final static String SERVER_URL = "http://vmnashira.bfh.ch:8080/HueLampApi/HueLampsApi";
        
    private static Subscriber s;
    //private static Publisher p;
    
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
            
            
            //http post request setup
            url = new URL(SERVER_URL);
            
            
        } catch (MqttException ex) {
            Logger.getLogger(BfhChLabDemAdHocHue.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(BfhChLabDemAdHocHue.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
    }
 
    
    public static void sendToHardware(String name, String command, String value){
        
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
            Logger.getLogger(BfhChLabDemAdHocHue.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    
    /**
     * enum containing the different client types
     */
    public enum ClientType{
        Subscriber,
        Publisher
    }
    
    public enum MQTTMessages{
        Online,
        Offline,
        Started
    }
    
    
}
