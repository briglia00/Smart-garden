package gardenservice;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.swing.Timer;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;


public class GardenService extends Thread {
	private enum IrrigationStatus {
		WAITING, RUNNING, STOPPING
	}
	
	private String MODE;
	private static final String serverMQTT = "tcp://broker.mqttdashboard.com:1883";
	public final String TOPIC_TEMPERATURE = "garden/temperature";
	public final String TOPIC_BRIGHTNESS = "garden/light";
	
	private String brightness;
	private String temperature;
	private IMqttClient publisher;
	private final SerialCommChannel channel;
	private final DashboardService ds;
	private IrrigationStatus irrstat;
	private static final List<String> IrrigationLevels = Collections.unmodifiableList(Arrays.asList("L", "M", "H"));
	private static final List<String> IrrigationLevelNames = 
			Collections.unmodifiableList(Arrays.asList("Low", "Medium", "High"));

	public GardenService(SerialCommChannel scc, DashboardService dashboard) throws MqttException{
		this.channel = scc;
		this.MODE = "AUTO";
		String publisherId = UUID.randomUUID().toString();
		this.brightness = "3"; //from 1 to 8
		this.temperature = "20"; //from 1 to 5
		this.publisher = new MqttClient(serverMQTT, publisherId);
		this.publisher.setCallback(new SubscribeCallback(this));
		this.publisher.connect();
		this.ds = dashboard;
		this.irrstat = IrrigationStatus.WAITING;
		System.out.println("Ready to receive messages.");
		this.publisher.subscribe(TOPIC_TEMPERATURE);
		this.publisher.subscribe(TOPIC_BRIGHTNESS);
	}

	public void run() {
		Timer timer = new Timer(8000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doCycle();
			}
		});
		timer.setRepeats(true);
		timer.start();
		while (true){
			if(this.channel.isMsgAvailable()){
				try {
					String msg = this.channel.receiveMsg();
					if (msg.contains("MODE")) {
						this.MODE = msg.substring(4);
						System.out.println();
					}
				} catch (InterruptedException e) {
					System.out.println("Error while receiving data");
				}
			}
		}
		
	}
	
	public void setTemperature(String temp) {
		this.temperature = temp;
	}
	
	public void setBrightness(String bright) {
		this.brightness = bright;
	}
	
	private void sendMessage(String s1, String s2) throws InterruptedException {
		if (!s1.equals("")) {
			Thread.sleep(500);
			this.channel.sendMsg(s1);
		}
		if (!s2.equals("")) {
			this.ds.sendToDashboard(s2);
		}
	}
	
	public void isRunning() {
		
	}
	
	private void doCycle() {
		int light;
		int temp;
		try {
			//Thread.sleep(8000);
			light = Integer.parseInt(this.brightness);
			temp = Integer.parseInt(this.temperature);
			int temp2 = (int)(temp / 10) + 1;
			//int realtemp = (temp*10)-10;
			int reallight = ((light-1) * 107) + 50;
			this.ds.sendToDashboard("temp;" + temp + "°C");
			this.ds.sendToDashboard("light;" + reallight + " lux");
			
			if (light < 5) {
	        	this.sendMessage("LM12ON", "LM12;ON");
	        	this.sendMessage("LM34LV" + Integer.toString(light),"LM34;Level of Intensity " + 
	        	Integer.toString(5-light) + " of 4");
			} else if (light >= 5) {
				this.sendMessage("LM12OF", "LM12;OFF");
				this.sendMessage("LM34LV0", "LM34;OFF");
			}
			/*if (temp >= 5) {
				this.sendMessage("GETIRR","");
				String msg = channel.receiveMsg();
				if(msg.equals("1")) {
					this.sendMessage("SETALM","");
				}
			} else*/ if (temp2 >= 2) {
				this.startIrrigation(temp2 - 2);
			} else if (light < 2) {
				this.startIrrigation(1);
			}
			System.out.println("temp: " + temp2 + " - light: " + light);
		} catch (NumberFormatException e1) {
    		System.out.println("Unreadable value");
    	} catch (InterruptedException e) {
    		System.out.println("Timer sleep exception");
		}
	}
	
	private void startIrrigation(int irrlvl) throws InterruptedException {
		if (this.irrstat == IrrigationStatus.WAITING) {
			Timer timer = new Timer(10000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						stopIrrigation();
					} catch (InterruptedException e) {
						System.out.println("Timer sleep exception");
					}
				}
			});
			timer.setRepeats(false);
			this.sendMessage("LVLIRR" + IrrigationLevels.get(irrlvl),"");
			this.sendMessage("STRIRR","IRR;Irrigation System Running at level " + IrrigationLevelNames.get(irrlvl));
			this.irrstat = IrrigationStatus.RUNNING;
			timer.start();
		}
	}
	
	private void stopIrrigation() throws InterruptedException {
		if (this.irrstat == IrrigationStatus.RUNNING) {
			Timer timer = new Timer(20000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					irrstat = IrrigationStatus.WAITING;
				}
			});
			timer.setRepeats(false);
			this.sendMessage("STPIRR","IRR;Irrigation System Stopped");
			this.irrstat = IrrigationStatus.STOPPING;
			timer.start();
		}
	}
	
	public static void main(String[] args) throws Exception {
    	SerialCommChannel channel = new SerialCommChannel("/dev/ttyACM0",9600);
    	DashboardService dashboard = new DashboardService(8001);
    	GardenService collector = new GardenService(channel, dashboard);
    	collector.start();
    }
}