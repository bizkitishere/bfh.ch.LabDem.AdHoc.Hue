/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bfh.ch.labdem.adhoc.hue.main;

//import bfh.ch.labdem.helper.LabDemLogger;

//import java.util.logging.Level;
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
    
    //client parameters
    private final MqttCallback MESSAGE_HANDLER = new MQTTMessageHandler();
    
    public Subscriber(String protocol, String broker, String port, String topic, String will, ClientType type) throws MqttException{
        super(protocol, broker, port, topic, will, type);
    }
    
    /**
     * subscribe to the subscribers topic
     * @throws MqttException 
     */
    public void subscribe() throws MqttException{
        mqttClient.setCallback(MESSAGE_HANDLER);
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
    class MQTTMessageHandler implements MqttCallback{

        @Override
        public void connectionLost(Throwable thrwbl) {
            //TODO implement
            System.out.println("Connection Lost...");
            System.out.println(thrwbl.getCause());
            System.out.println(thrwbl.getMessage());
            String m = Subscriber.class.getName() + "\n" + thrwbl.getMessage();
            //LabDemLogger.LOGGER.log(Level.SEVERE, m);
        }

        @Override
        //messega that is called when a new mqtt message arrives
        public void messageArrived(String string, MqttMessage mm) throws MqttException {
            //TODO implement
            
            //System.out.printf("Topic: (%s) Payload: (%s) Retained: (%b) \n", string, new String(mm.getPayload()), mm.isRetained());
            
                                                //typeId, HW name, command, value
            //messages need to be in the format: "[int], [String], [String], [String]"
            
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
                
                messageId = Integer.parseInt(tokens[4]);
                
                BfhChLabDemAdHocHue.sendToHardware(hwName, command, value, messageId);
                
            }catch (NumberFormatException ex){
                String m = Subscriber.class.getName() + " - " + ex.getMessage();
                //LabDemLogger.LOGGER.log(Level.WARNING, m);
            }
                
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken imdt) {
            //TODO implement
            System.out.println("Delivery Complete...");
        }
        
    }
    
}
