#include <WiFi.h>
#include <PubSubClient.h>
#define MSG_BUFFER_SIZE  50

/* wifi network info */

const char* ssid = "TIM-25873012";
const char* password = "O204O4M2YEUQzQiHLjHLsj6D";
const int port = 1883;

/* MQTT server address */
const char* mqtt_server = "broker.mqttdashboard.com";

/* MQTT client management */

WiFiClient espClient;
PubSubClient client(espClient);


unsigned long lastMsgTime = 0;
char msg[MSG_BUFFER_SIZE];
int value = 0;

void setup_wifi() {

  delay(10);

  Serial.println(String("Connecting to ") + ssid);

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

/* MQTT subscribing callback */

void callback(char* topic, byte* payload, unsigned int length) {
  Serial.println(String("Message arrived on [") + topic + "] len: " + length );
}

void reconnect() {
  
  // Loop until we're reconnected
  
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    
    // Create a random client ID
    String clientId = String("garden-sensorboard"); //+String(random(0xffff), HEX);

    // Attempt to connect
    if (client.connect(clientId.c_str())) {
      Serial.println("connected");
      // Once connected, publish an announcement...
      // client.publish("outTopic", "hello world");
      // ... and resubscribe
      client.subscribe("garden/temperature");
      client.subscribe("garden/light");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(", trying again in 5 seconds");
      delay(5000);
    }
  }
}

void setup() {
  Serial.begin(115200);
  setup_wifi();
  randomSeed(micros());
  client.setServer(mqtt_server, port);
  client.setCallback(callback);
}

void loop() {

  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  unsigned long now = millis();
  if (now - lastMsgTime > 10000) {
    lastMsgTime = now;
    value++;

    /* creating a msg in the buffer */
    snprintf (msg, MSG_BUFFER_SIZE, "%ld", map(random(100), 0, 100, 1, 5));
    
    client.publish("garden/temperature", msg);  

    snprintf (msg, MSG_BUFFER_SIZE, "%ld", map(random(100), 0, 100, 1, 8));
    
    client.publish("garden/light", msg);
  }
}
