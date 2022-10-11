#include <WiFi.h>
#include <PubSubClient.h>
#include <DHT.h>


#define MSG_BUFFER_SIZE  50
#define DHTPIN 4
#define LEDPIN 5
#define TEMPPIN 35
#define DHTTYPE DHT11

/* wifi network info */
const char* ssid = "TIM-25873012";
const char* password = "O204O4M2YEUQzQiHLjHLsj6D";
const int port = 1883;

/* MQTT server address */
const char* mqtt_server = "broker.mqttdashboard.com";

/* MQTT client management */
WiFiClient espClient;
PubSubClient client(espClient);

/* DHT Sensor*/
DHT dht(DHTPIN, DHTTYPE);

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
  Serial.println(String("Message arrived on [") + topic + "]");
  if (String(topic) == "garden/alarm"){
    String received = "";
    for (int i = 0; i < length; i++) {
      received = received + (char)payload[i];
    }
    if (received == "0"){
      digitalWrite(LEDPIN, LOW);
    } else if (received == "1"){
      digitalWrite(LEDPIN, HIGH);
    }
  }  
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    
    // Create a random client ID
    String clientId = String("garden-sensorboard" + String(random(0xffff), HEX));

    // Attempt to connect
    if (client.connect(clientId.c_str())) {
      Serial.println("connected");
      client.subscribe("garden/temperature");
      client.subscribe("garden/light");
      client.subscribe("garden/alarm");
      digitalWrite(LEDPIN, HIGH);
    } else {
      digitalWrite(LEDPIN, LOW);
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
  dht.begin();
  pinMode(LEDPIN, OUTPUT);
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

    float temp = dht.readTemperature();
    if(!isnan(temp) && temp < 50 && temp > 0){
      int temperature = round(dht.readTemperature());
      snprintf (msg, MSG_BUFFER_SIZE, "%ld", temperature);
      client.publish("garden/temperature", msg);  
    }

    int light = analogRead(TEMPPIN);

    snprintf (msg, MSG_BUFFER_SIZE, "%ld", map(light, 0, 4095, 1, 8));
    
    client.publish("garden/light", msg);
  }
}
