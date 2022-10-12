package gardenservice;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.swing.Timer;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;


public class GardenService extends Thread {
	private enum IrrigationStatus {
		WAITING, RUNNING, STOPPING
	}
	
	private String MODE;
	private static final String serverMQTT = "tcp://broker.mqttdashboard.com:1883";
	public final String TOPIC_TEMPERATURE = "garden/temperature";
	public final String TOPIC_BRIGHTNESS = "garden/light";
	public final String TOPIC_ALARM = "garden/alarm";
	
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
		this.brightness = null; //from 1 to 8
		this.temperature = null; //from 1 to 5
		this.publisher = new MqttClient(serverMQTT, publisherId);
		this.publisher.setCallback(new MqttCallback(){
			@Override
		    public void connectionLost(Throwable cause) {}

		    @Override
		    public void messageArrived(String topic, MqttMessage message) {
		         //System.out.println("Message arrived. Topic: " + topic + " Message: " + message.toString());
		         /*if ("home/LWT".equals(topic)){
		              System.err.println("Sensor gone!");
		         }*/
		         if (TOPIC_BRIGHTNESS.equals(topic)) {
		        	 brightness = message.toString();
		         }
		         if (TOPIC_TEMPERATURE.equals(topic)) {
		        	 temperature = message.toString();
		         }
		    }
		    
		    @Override
		    public void deliveryComplete(IMqttDeliveryToken token) {}
		});
		this.publisher.connect();
		
		this.ds = dashboard;
		this.irrstat = IrrigationStatus.WAITING;
		System.out.println("Ready to receive messages.");
		this.publisher.subscribe(TOPIC_TEMPERATURE);
		this.publisher.subscribe(TOPIC_BRIGHTNESS);
	}

	public void run() {
		while (true){
			if(this.channel.isMsgAvailable()){
				try {
					String msg = this.channel.receiveMsg();
					if (msg.contains("MODE")) {
						switchToMode(msg.substring(4));
					}
				} catch (InterruptedException e) {
					System.out.println("Error while receiving data");
				}
			}
			if(this.brightness != null && this.temperature != null && this.MODE.equals("AUTO")) {
				this.doCycle();
			}
		}
		
	}
	
	private void sendMessage(String s1) throws InterruptedException {
		Thread.sleep(100);
		this.channel.sendMsg(s1);
	}
	
	private void doCycle() {
		int light;
		int temp;
		try {
			light = Integer.parseInt(this.brightness);
			temp = Integer.parseInt(this.temperature);
			this.temperature = null; this.brightness = null;
			int temp2 = (int)(temp / 10) + 1;
			//int realtemp = (temp*10)-10;
			int reallight = ((light-1) * 107) + 50;
			this.ds.sendToDashboard("temp;" + temp + "°C");
			this.ds.sendToDashboard("light;" + reallight + " lux");
			
			if (light < 5) {
	        	this.sendMessage("LM12ON");
	        	this.ds.sendToDashboard("LM12;ON");
	        	this.sendMessage("LM34LV" + Integer.toString(light));
	        	this.ds.sendToDashboard("LM34;Level of Intensity " + Integer.toString(5-light) + " of 4");
			} else if (light >= 5) {
				this.sendMessage("LM12OF");
				this.ds.sendToDashboard("LM12;OFF");
				this.sendMessage("LM34LV0");
				this.ds.sendToDashboard("LM34;OFF");
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
			this.sendMessage("LVLIRR" + IrrigationLevels.get(irrlvl));
			this.sendMessage("STRIRR");
			this.ds.sendToDashboard("IRR;Irrigation System Running at level " + IrrigationLevelNames.get(irrlvl));
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
			this.sendMessage("STPIRR");
			this.ds.sendToDashboard("IRR;Irrigation System Stopped");
			this.irrstat = IrrigationStatus.STOPPING;
			timer.start();
		}
	}
	
	private void switchToMode(String newmode) {
		try {
			if(newmode.contains("ALARM")) {
				MqttMessage mqttmsg = new MqttMessage("1".getBytes());
				mqttmsg.setQos(0);
				this.publisher.publish(TOPIC_ALARM, mqttmsg);
			} else if(this.MODE == "ALARM" && !newmode.contains("ALARM")) {
				MqttMessage mqttmsg = new MqttMessage("0".getBytes());
				mqttmsg.setQos(0);
				this.publisher.publish(TOPIC_ALARM, mqttmsg);
			}
		} catch (MqttPersistenceException e1) {
			System.out.println("Error while Sending alarm message to esp");
		} catch (MqttException e2) {
			System.out.println("Error while Sending alarm message to esp");
		}
		this.MODE = newmode;
		this.ds.sendToDashboard("MODE;" + newmode);
	}
	
	public static void main(String[] args) throws Exception {
    	SerialCommChannel channel = new SerialCommChannel("/dev/ttyACM0",9600);
    	DashboardService dashboard = new DashboardService(8001);
    	GardenService collector = new GardenService(channel, dashboard);
    	Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				System.out.println("Performing shutdown");
			}
		});
    	collector.start();
    }
}