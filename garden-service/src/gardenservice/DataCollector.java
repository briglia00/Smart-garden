package gardenservice;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;


public class DataCollector extends Thread {
	private static final String serverMQTT = "tcp://broker.mqttdashboard.com:1883";
	public final String TOPIC_TEMPERATURE = "garden/temperature";
	public final String TOPIC_BRIGHTNESS = "garden/light";
	
	private String brightness;
	private String temperature;
	private IMqttClient publisher;
	private final SerialCommChannel channel;
	private static String MODE;
	
	
	public DataCollector(SerialCommChannel scc) throws MqttException{
		this.channel = scc;
		this.MODE = "AUTO";
		String publisherId = UUID.randomUUID().toString();
		this.brightness = "3"; //from 1 to 8
		this.temperature = "20"; //from 1 to 5
		this.publisher = new MqttClient(serverMQTT, publisherId);
		this.publisher.setCallback(new SubscribeCallback(this));
		this.publisher.connect();
		System.out.println("Ready to receive messages.");
		this.publisher.subscribe(TOPIC_TEMPERATURE);
		this.publisher.subscribe(TOPIC_BRIGHTNESS);
	}
	public void run() {
		while (true){
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				System.out.println("Timer sleep exception");
			}
			if (Integer.parseInt(this.brightness) < 5) {
				try {
	        		channel.sendMsg("LM12ON");
	        		channel.sendMsg("LM34LV" + this.brightness);
	        		System.out.println("LM34LV" + this.brightness);
	        	} catch (Exception e1) {
	        		System.out.println("error while sending message to arduino");
	        	}
				if (Integer.parseInt(this.brightness) < 2) {
					channel.sendMsg("STRIRR");
				}
			}
			if (Integer.parseInt(this.temperature) > 35) {
				channel.sendMsg("STRIRR");
				channel.sendMsg("LVLIRRH");
			}
			if (Integer.parseInt(this.brightness) > 5) {
				channel.sendMsg("STPIRR");
			}
			//System.out.println(this.temperature + " - " + this.brightness);
			/*try {
        		channel.sendMsg("RECOVR");
        	} catch (Exception e1) {
        		
        	}*/
			
			
			
			/*try {
				this.publisher.subscribe("temperature/temperature", (topic, msg) -> {
				    byte[] payload = msg.getPayload();
				    System.out.println(payload);
				    // ... payload handling omitted
				});
				
			} catch (MqttException e) {
				System.out.println("Client non connesso");
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				System.out.println("Timer sleep exception");
			}*/
		}
	}
	
	public void setTemperature(String temp) {
		this.temperature = temp;
	}
	
	public void setBrightness(String bright) {
		this.brightness = bright;
	}
	
	public void isRunning() {
		
	}
}