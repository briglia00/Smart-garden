package gardenservice;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SubscribeCallback implements MqttCallback {
	private GardenService dc;
	
	SubscribeCallback(GardenService dc){
		this.dc = dc;
	}

    @Override
    public void connectionLost(Throwable cause) {}

    @Override
    public void messageArrived(String topic, MqttMessage message) {
         //System.out.println("Message arrived. Topic: " + topic + " Message: " + message.toString());
         if ("home/LWT".equals(topic)){
              System.err.println("Sensor gone!");
         } 
         if (dc.TOPIC_BRIGHTNESS.equals(topic)) {
        	 this.dc.setBrightness(message.toString());
         }
         if (dc.TOPIC_TEMPERATURE.equals(topic)) {
        	 this.dc.setTemperature(message.toString());
         }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}

}
