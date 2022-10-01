package gardenservice;

import java.awt.Color;
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
		int light;
		int temp;
		while (true){
			try {
				Thread.sleep(8000);
				light = Integer.parseInt(this.brightness);
				temp = Integer.parseInt(this.temperature);
				
				if (light < 5) {
		        	channel.sendMsg("LM12ON"); Thread.sleep(500);
		        	channel.sendMsg("LM34LV" + Integer.toString(light));
					if (light < 2) {
						Thread.sleep(500);
						channel.sendMsg("STRIRR");
					}
				} else if (light >= 5) {
					Thread.sleep(500);
					channel.sendMsg("LM12OF"); Thread.sleep(500);
	        		channel.sendMsg("LM34LV0");
				}
				if (temp == 3) {
					Thread.sleep(500);
					channel.sendMsg("LVLIRRL"); Thread.sleep(500);
					channel.sendMsg("STRIRR");
				} else if (temp == 4) {
					Thread.sleep(500);
					channel.sendMsg("LVLIRRM"); Thread.sleep(500);
					channel.sendMsg("STRIRR"); 
				} else if (temp == 5) {
					Thread.sleep(500);
					channel.sendMsg("LVLIRRH"); Thread.sleep(500);
					channel.sendMsg("STRIRR");
				} else if (temp == 6) {
					Thread.sleep(500);
					channel.sendMsg("GETIRR");
					String msg = channel.receiveMsg();
					if(msg.equals("1")) {
						Thread.sleep(500); channel.sendMsg("SETALM");
					}
				}
				System.out.println("temp: " + temp + " - light: " + light);
			} catch (NumberFormatException e1) {
        		System.out.println("Unreadable value");
        	} catch (InterruptedException e) {
        		System.out.println("Timer sleep exception");
			}
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