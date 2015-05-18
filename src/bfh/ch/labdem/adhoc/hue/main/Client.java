/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bfh.ch.labdem.adhoc.hue.main;

import bfh.ch.labdem.adhoc.hue.main.BfhChLabDemAdHocHue.ClientType;
import java.net.URI;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Parent Class for MQTT clients, offers some basic functionality and required parameters
 * @author Philippe Lüthi, Elia Kocher
 */
public abstract class Client {
    
    //parameters to connect to broker
    final ClientType TYPE;
    final String PROTOCOL;
    final String BROKER;
    final String PORT;
    final URI BROKER_URI;
    final String CON_ID;
    final String TOPIC;
    final String WILL;
    
    //client parameters
    final MqttClient mqttClient;
    MqttCallback msgHandler;
    
    
    /**
     * 
     * @param protocol protocol to use
     * @param broker broker name
     * @param port port to connect
     * @param topic topic to publish/subscribe to
     * @param will message to send when connection is interrupted
     * @param type client type
     * @throws MqttException 
     */
    public Client(String protocol, String broker, String port, String topic, String will, ClientType type) throws MqttException{
        this.PROTOCOL = protocol;
        this.BROKER = broker;
        this.PORT = port;
        this.TOPIC = topic;
        this.WILL = will;
        this.TYPE = type;
        this.BROKER_URI = URI.create(protocol + "://" + broker + ":" + port);
        this.CON_ID = broker + "." + topic + "." + "Server" + "." + TYPE.toString();
        
        mqttClient = new MqttClient(BROKER_URI.toString(), CON_ID);
    }
    
    /**
     * opens the connection to the broker and sets the will
     * @throws MqttException 
     */
    public void connectToBroker() throws MqttException {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setWill(TOPIC, WILL.getBytes(), 1, true);
        connectOptions.setCleanSession(true);
	mqttClient.connect(connectOptions);
    }

    /**
     * closes the connection to the broker
     * @throws MqttException 
     */
    public void disconnectFromBroker() throws MqttException {
	mqttClient.disconnect();
    }
    
}
