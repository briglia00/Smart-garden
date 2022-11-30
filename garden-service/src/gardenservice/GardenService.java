package gardenservice;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
	
	private final int IRR_RUNNING_TIME = 10000;
	private final int IRR_WAITING_TIME = 30000;
	private static final String serverMQTT = "tcp://broker.mqttdashboard.com:1883";
	public final String TOPIC_TEMPERATURE = "garden/temperature";
	public final String TOPIC_BRIGHTNESS = "garden/light";
	public final String TOPIC_ALARM = "garden/alarm";
	
	private String MODE;
	private String brightness;
	private String temperature;
	private IMqttClient publisher;
	private final SerialCommChannel channel;
	private final DashboardService ds;
	private IrrigationStatus irrstat;
	private int irrspeed;
	private static final List<String> IrrigationLevels = 
			Collections.unmodifiableList(Arrays.asList("L", "M", "H"));
	private static final List<String> IrrigationLevelNames = 
			Collections.unmodifiableList(Arrays.asList("Low", "Medium", "High"));
	private final Map<String, String> serialToDash = Map.ofEntries(
			  new AbstractMap.SimpleEntry<String, String>("LM1ON", "LM1;ON"),
			  new AbstractMap.SimpleEntry<String, String>("LM1OF", "LM1;OFF"),
			  new AbstractMap.SimpleEntry<String, String>("LM2ON", "LM2;ON"),
			  new AbstractMap.SimpleEntry<String, String>("LM2OF", "LM2;OFF"),
			  new AbstractMap.SimpleEntry<String, String>("STPIRR", "IRR;Irrigation System Stopped"),
			  new AbstractMap.SimpleEntry<String, String>("LM3LV0", "LM3;OFF"),
			  new AbstractMap.SimpleEntry<String, String>("LM3LV1", "LM3;Level of Intensity 1 of 4"),
			  new AbstractMap.SimpleEntry<String, String>("LM3LV2", "LM3;Level of Intensity 2 of 4"),
			  new AbstractMap.SimpleEntry<String, String>("LM3LV3", "LM3;Level of Intensity 3 of 4"),
			  new AbstractMap.SimpleEntry<String, String>("LM3LV4", "LM3;Level of Intensity 4 of 4"),
			  new AbstractMap.SimpleEntry<String, String>("LM4LV0", "LM4;OFF"),
			  new AbstractMap.SimpleEntry<String, String>("LM4LV1", "LM4;Level of Intensity 1 of 4"),
			  new AbstractMap.SimpleEntry<String, String>("LM4LV2", "LM4;Level of Intensity 2 of 4"),
			  new AbstractMap.SimpleEntry<String, String>("LM4LV3", "LM4;Level of Intensity 3 of 4"),
			  new AbstractMap.SimpleEntry<String, String>("LM4LV4", "LM4;Level of Intensity 4 of 4")
			  );

	public GardenService(SerialCommChannel scc, DashboardService dashboard) throws MqttException{
		this.channel = scc;
		this.MODE = "AUTO";
		String publisherId = UUID.randomUUID().toString();
		this.brightness = null; //from 1 to 8
		this.temperature = null; //from 1 to 5
		this.irrspeed = 2;
		this.publisher = new MqttClient(serverMQTT, publisherId);
		this.publisher.setCallback(new MqttCallback(){
			@Override
		    public void connectionLost(Throwable cause) {}

		    @Override
		    public void messageArrived(String topic, MqttMessage message) {
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
		this.ds.start();
		this.irrstat = IrrigationStatus.WAITING;
		
		this.publisher.subscribe(TOPIC_TEMPERATURE);
		this.publisher.subscribe(TOPIC_BRIGHTNESS);
		this.publisher.subscribe("garden/debug");
		MqttMessage mqttmsg = new MqttMessage("1".getBytes());
		mqttmsg.setQos(0);
		this.publisher.publish(TOPIC_ALARM, mqttmsg);
		
		System.out.println("Ready to receive data.");
	}

	public void run() {
		while (true){
			if(this.channel.isMsgAvailable()){
				try {
					String msg = this.channel.receiveMsg();
					if (msg.contains("MODE")) {
						switchToMode(msg.substring(4));
					} 
					if(msg.contains("LVLIRR")) {
						irrspeed = IrrigationLevels.indexOf(msg.substring(6, 7));
					} else if(serialToDash.containsKey(msg)) {
						this.ds.sendToDashboard(serialToDash.get(msg));
					} else if(msg.contains("STRIRR")) {
						this.ds.sendToDashboard("IRR;Irrigation System Running at level " + 
					IrrigationLevelNames.get(irrspeed));
					}
				} catch (InterruptedException e) {
					System.out.println("Error while receiving data");
				} catch (IndexOutOfBoundsException e2) {
					System.out.println("Error while checking irrigation speed");
				}
			}
			if(this.brightness != null && this.temperature != null) {
				try {
					int light = Integer.parseInt(this.brightness);
					int temp = Integer.parseInt(this.temperature);
					
					this.temperature = null; this.brightness = null;
					int temp2 = (int)(temp / 10) + 1;
					int reallight = ((light-1) * 107) + 50;
					this.ds.sendToDashboard("temp;" + temp + "°C");
					this.ds.sendToDashboard("light;" + reallight + " lux");
					
					if(this.MODE.equals("AUTO")){
						this.doCycle(temp2, light);
					}
				} catch (NumberFormatException e1) {
		    		System.out.println("Unreadable value");
		    	} catch (InterruptedException e) {
		    		System.out.println("Timer sleep exception");
				}
			}
		}
		
	}
	
	private void sendSerialMessage(String s1) throws InterruptedException {
		Thread.sleep(150);
		this.channel.sendMsg(s1);
	}
	
	private void doCycle(int temp, int light) throws InterruptedException {
		System.out.println("temperature: " + temp + "/5 - light: " + light + "/8");
		if (light < 5) {
        	this.sendSerialMessage("LM12ON");
        	this.ds.sendToDashboard("LM1;ON");
        	this.ds.sendToDashboard("LM2;ON");
        	this.sendSerialMessage("LM3LV" + Integer.toString(5-light));
        	this.sendSerialMessage("LM4LV" + Integer.toString(5-light));
        	this.ds.sendToDashboard("LM3;Level of Intensity " + Integer.toString(5-light) + " of 4");
        	this.ds.sendToDashboard("LM4;Level of Intensity " + Integer.toString(5-light) + " of 4");
		} else if (light >= 5) {
			this.sendSerialMessage("LM12OF");
			this.ds.sendToDashboard("LM1;OFF");
			this.ds.sendToDashboard("LM2;OFF");
			this.sendSerialMessage("LM3LV0");
			this.sendSerialMessage("LM4LV0");
			this.ds.sendToDashboard("LM3;OFF");
			this.ds.sendToDashboard("LM4;OFF");
		}
		if (temp >= 5) {
			this.startIrrigation(2);
			if (this.irrstat.equals(IrrigationStatus.STOPPING)) {
				this.switchToMode("ALARM");
			}
		} else if (temp >= 2) {
			this.startIrrigation(temp - 2);
		} else if (light < 2) {
			this.startIrrigation(1);
		}
	}
	
	private void startIrrigation(int irrlvl) throws InterruptedException {
		if (this.irrstat.equals(IrrigationStatus.WAITING)) {
			Timer timer = new Timer(IRR_RUNNING_TIME, new ActionListener() {
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
			this.sendSerialMessage("LVLIRR" + IrrigationLevels.get(irrlvl));
			this.sendSerialMessage("STRIRR");
			this.irrspeed = irrlvl;
			this.ds.sendToDashboard("IRR;Irrigation System Running at level " + IrrigationLevelNames.get(irrlvl));
			this.irrstat = IrrigationStatus.RUNNING;
			timer.start();
		}
	}
	
	private void stopIrrigation() throws InterruptedException {
		if (this.irrstat == IrrigationStatus.RUNNING) {
			Timer timer = new Timer(IRR_WAITING_TIME, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					irrstat = IrrigationStatus.WAITING;
				}
			});
			timer.setRepeats(false);
			this.sendSerialMessage("STPIRR");
			this.ds.sendToDashboard("IRR;Irrigation System Stopped");
			this.irrstat = IrrigationStatus.STOPPING;
			timer.start();
		}
	}
	
	private void switchToMode(String newmode) throws InterruptedException {
		try {
			System.out.println("Switching to mode " + newmode);
			if(newmode.equals("ALARM")) {
				MqttMessage mqttmsg = new MqttMessage("0".getBytes());
				mqttmsg.setQos(0);
				this.publisher.publish(TOPIC_ALARM, mqttmsg);
				this.sendSerialMessage("SETALM");
			} else if(this.MODE.equals("ALARM") && !newmode.equals("ALARM")) {
				MqttMessage mqttmsg = new MqttMessage("1".getBytes());
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
    	collector.start();
    	Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				System.out.println("Performing shutdown");
				channel.close();
			}
		});
    }
}
