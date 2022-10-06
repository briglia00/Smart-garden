package gardenservice;


import java.util.Arrays;
import java.util.Collections;
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
	private final DashboardService ds;
	private static final List<String> IrrigationLevels = Collections.unmodifiableList(Arrays.asList("L", "M", "H"));
	//private static String MODE;
	
	
	public DataCollector(SerialCommChannel scc, DashboardService dashboard) throws MqttException{
		this.channel = scc;
		//this.MODE = "AUTO";
		String publisherId = UUID.randomUUID().toString();
		this.brightness = "3"; //from 1 to 8
		this.temperature = "20"; //from 1 to 5
		this.publisher = new MqttClient(serverMQTT, publisherId);
		this.publisher.setCallback(new SubscribeCallback(this));
		this.publisher.connect();
		this.ds = dashboard;
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
				
				int realtemp = (temp*10)-10;
				int reallight = ((light-1) * 107) + 50;
				this.ds.sendToDashboard("temp;" + realtemp + "°C");
				this.ds.sendToDashboard("light;" + reallight + " lux");
				
				if (light < 5) {
		        	this.sendMessage("LM12ON");
		        	this.ds.sendToDashboard("LM12;ON");
		        	this.sendMessage("LM34LV" + Integer.toString(light));
		        	this.ds.sendToDashboard("LM34;Level of Intensity " + Integer.toString(5-light) + " of 4");
					if (light < 2) {
						this.sendMessage("STRIRR");
					}
				} else if (light >= 5) {
					this.sendMessage("LM12OF");
					this.ds.sendToDashboard("LM12;OFF");
					this.sendMessage("LM34LV0");
					this.ds.sendToDashboard("LM34;OFF");
				}
				if (temp >= 6) {
					this.sendMessage("GETIRR");
					String msg = channel.receiveMsg();
					if(msg.equals("1")) {
						this.sendMessage("SETALM");
					}
				} else if (temp >= 2) {
					this.sendMessage("LVLIRR" + IrrigationLevels.get(temp - 2));
					this.sendMessage("STRIRR");
				}
				
				/*
				if (temp == 3) {
					this.sendMessage("LVLIRRL");
					this.sendMessage("STRIRR");
				} else if (temp == 4) {
					this.sendMessage("LVLIRRM");
					this.sendMessage("STRIRR"); 
				} else if (temp == 5) {
					this.sendMessage("LVLIRRH");
					this.sendMessage("STRIRR");
				}*/
				/*Timer timer = new Timer(3000, new ActionListener() {
  @Override
  public void actionPerformed(ActionEvent arg0) {
    // Code to be executed
  }
});
timer.setRepeats(false); // Only execute once
timer.start(); // Go go go!*/
				//System.out.println("temp: " + temp + " - light: " + light);
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
	
	public void sendMessage(String s) throws InterruptedException {
		Thread.sleep(500);
		this.channel.sendMsg(s);
	}
	
	public void isRunning() {
		
	}
	
	public static void main(String[] args) throws Exception {
    	SerialCommChannel channel = new SerialCommChannel("/dev/ttyACM0",9600);
    	DashboardService dashboard = new DashboardService(8001);
    	DataCollector collector = new DataCollector(channel, dashboard);
    	collector.start();
    	//new GardenService(5, channel);
    }
}