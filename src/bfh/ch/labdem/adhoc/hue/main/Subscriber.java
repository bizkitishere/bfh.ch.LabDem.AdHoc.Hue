/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bfh.ch.labdem.adhoc.hue.main;

import bfh.ch.labdem.adhoc.hue.main.BfhChLabDemAdHocHue.ClientType;
import bfh.ch.labdem.adhoc.hue.main.BfhChLabDemAdHocHue.MQTTMessages;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Used to receive messages from a MQTT broker
 * 
 * @author Philippe Lüthi, Elia Kocher
 */
public class Subscriber extends Client {
    
    public Subscriber(String protocol, String broker, String port, String topic, String will, ClientType type) throws MqttException{
        super(protocol, broker, port, topic, will, type);
        msgHandler = new MQTTMessageHandler();
    }
    
    /**
     * subscribe to the subscribers topic
     * @throws MqttException 
     */
    public void subscribe() throws MqttException{
        mqttClient.setCallback(msgHandler);
        mqttClient.subscribe(TOPIC);
    }
    
    /**
     * unsubscribe from the subscribers topic
     * @throws MqttException 
     */
    public void unsubscribe() throws MqttException{
        mqttClient.setCallback(null);
        mqttClient.unsubscribe(TOPIC);
    }

    /**
     * Handles the arriving messages, connection loss and complete delivery
     */
    private class MQTTMessageHandler implements MqttCallback{

        @Override
        public void connectionLost(Throwable thrwbl) {
            //ad hoc will just shut down when the connection is lost
            //this will notify daemon and app
            System.exit(1);
        }

        @Override
        //messega that is called when a new mqtt message arrives
        public void messageArrived(String string, MqttMessage mm) throws MqttException {            
                                                //typeId, HW name, command, value,   messageId
            //messages need to be in the format: "[int], [String], [String], [String], [int]"
            
            int typeId, messageId;
            String hwName, command, value;

            String message = new String(mm.getPayload());
            
            //nothing to do if the "Online" message is received
            if(message.equals(MQTTMessages.Online.toString())) return;
            
            String[] tokens = message.split(";", -1);
            
            try{
                typeId = Integer.parseInt(tokens[0]);
                
                //only do something if type is 1 -> HUE lamps
                if(typeId != BfhChLabDemAdHocHue.HARDWARE_TYPE_ID) return;
                
                hwName = tokens[1];
                command = tokens[2];
                value = tokens[3];
                
                //used to "group" messages that belong to the same action
                messageId = Integer.parseInt(tokens[4]);
                
                BfhChLabDemAdHocHue.sendToHardware(hwName, command, value, messageId);
                
            }catch (NumberFormatException ex){
                //nothing to do when the format is not correct
            }       
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken imdt) {
            //not needed, since this class will only receive messages
        }
        
    }
    
}
