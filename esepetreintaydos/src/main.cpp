#include <HTTPClient.h>
#include "ArduinoJson.h"
#include <WiFiUdp.h>
#include <PubSubClient.h>
#include <MQSpaceData.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <Ethernet.h>

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
#define MQ9_PIN 35


Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

float co=0.0;
float ch4 = 0.0;
float lpg = 0.0;
float Ro=0.0;


MQSpaceData mq9(12, MQ9_PIN);
// Replace 0 by ID of this current device
const int DEVICE_ID = 0;

int test_delay = 1000; // so we don't spam the API
boolean describe_tests = true;

// Replace 0.0.0.0 by your server local IP (ipconfig [windows] or ifconfig [Linux o MacOS] gets IP assigned to your PC)
String serverName = "http://192.168.66.209:8080/";
HTTPClient http;

// Replace WifiName and WifiPassword by your WiFi credentials
#define STASSID "rafa_cuadrado"    //"Your_Wifi_SSID"
#define STAPSK "03102003RIcs" //"Your_Wifi_PASSWORD"

// MQTT configuration
WiFiClient espClient;
PubSubClient client(espClient);

// Server IP, where de MQTT broker is deployed
const char *MQTT_BROKER_ADRESS = "192.168.66.209";
const uint16_t MQTT_PORT = 1883;

// Name for this MQTT client
const char *MQTT_CLIENT_NAME = "ArduinoClient_0";

// callback a ejecutar cuando se recibe un mensaje
// en este ejemplo, muestra por serial el mensaje recibido
void initOLED() {
  Wire.begin(21,22);
  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println(F("Fallo en OLED"));
    while (true);
  }
  Serial.println("Fallo en OLED");
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.println("Pantalla lista");
  display.display();
  delay(3000);
  display.clearDisplay();
  display.println("Pantalla NO lista");
  display.display();

}
void OnMqttReceived(char *topic, byte *payload, unsigned int length)
{
  Serial.print("Received on ");
  Serial.print(topic);
  Serial.print(": ");

  String content = "";
  for (size_t i = 0; i < length; i++)
  {
    content.concat((char)payload[i]);
  }

  Serial.print(content);
  Serial.println();
  if(topic=="esp32/actuador/OLED"){
    display.clearDisplay();
    display.setCursor(0, 0);
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.println(content);
    display.setCursor(0, 8);
    display.display();
  }else if(topic=="esp32/actuador/BOCINA"){
    Serial.println("bla bla bla");
  }
  
}

// inicia la comunicacion MQTT
// inicia establece el servidor y el callback al recibir un mensaje
void InitMqtt()
{
  client.setServer(MQTT_BROKER_ADRESS, MQTT_PORT);
  client.setCallback(OnMqttReceived);
}




// Setup
void setup()
{
  Serial.begin(9600);
  // Configuración y calibración del MQ-9
  mq9.setVoltage(3.3);      // Voltaje según cómo lo alimentes (3.3V o 5V)
  mq9.setRange(100);        // Cuántas muestras se promedian
  mq9.solderedRL();         // RL soldada de 1 kΩ
  mq9.RSRoMQAir(9.6);       // Relación Rs/Ro en aire limpio (valor típico)

  Ro = mq9.calculateRo();  // ¡Calibra aquí!
  Serial.print("Ro calibrado: ");
  Serial.println(Ro);

  // Después de esto, puedes establecerlo manualmente si lo guardas
  
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(STASSID);
  mq9.begin();
  mq9.setVoltage(3.3);    
  mq9.setRange(100);      
  mq9.solderedRL();
  initOLED();
  /* Explicitly set the ESP32 to be a WiFi-client, otherwise, it by default,
     would try to act as both a client and an access-point and could cause
     network-issues with your other WiFi-devices on your WiFi-network. */
  WiFi.mode(WIFI_STA);
  WiFi.begin(STASSID, STAPSK);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }

  InitMqtt();

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Setup!");
}

// conecta o reconecta al MQTT
// consigue conectar -> suscribe a topic y publica un mensaje
// no -> espera 5 segundos
void ConnectMqtt()
{
  Serial.print("Starting MQTT connection...");
  if (client.connect(MQTT_CLIENT_NAME))
  {
    client.subscribe("esp32/actuador/OLED");
    client.publish("esp32/actuador/OLED", "connected");
    client.subscribe("esp32/actuador/BOCINA");
    client.publish("esp32/actuador/BOCINA", "connected");
  }
  else
  {
    Serial.print("Failed MQTT connection, rc=");
    Serial.print(client.state());
    Serial.println(" try again in 5 seconds");

    delay(5000);
  }
}

// gestiona la comunicación MQTT
// comprueba que el cliente está conectado
// no -> intenta reconectar
// si -> llama al MQTT loop
void HandleMqtt()
{
  if (!client.connected())
  {
    ConnectMqtt();
  }
  client.loop();
}

String response;

String serializeSensorValueBody(int idSensor, long timestamp, float value)
{
  // StaticJsonObject allocates memory on the stack, it can be
  // replaced by DynamicJsonDocument which allocates in the heap.
  //
  DynamicJsonDocument doc(2048);

  // Add values in the document
  //
  doc["sensorId"] = idSensor;
  doc["timestamp"] = timestamp;
  doc["value"] = value;

  // Generate the minified JSON and send it to the Serial port.
  //
  String output;
  serializeJson(doc, output);
  Serial.println(output);

  return output;
}

String serializeActuatorStatusBody(float status, bool statusBinary, int idActuator, long timestamp)
{
  DynamicJsonDocument doc(2048);

  doc["status"] = status;
  doc["statusBinary"] = statusBinary;
  doc["idActuator"] = idActuator;
  doc["timestamp"] = timestamp;

  String output;
  serializeJson(doc, output);
  return output;
}

String serializeDeviceBody(String deviceSerialId, String name, String mqttChannel, int idGroup)
{
  DynamicJsonDocument doc(2048);

  doc["deviceSerialId"] = deviceSerialId;
  doc["name"] = name;
  doc["mqttChannel"] = mqttChannel;
  doc["idGroup"] = idGroup;

  String output;
  serializeJson(doc, output);
  return output;
}
void leerMQ9() {
  co = mq9.MQ9DataCO();     // Monóxido de carbono (ppm) limite 2000
  ch4 = mq9.MQ9DataCH4();   // Metano (ppm) limite 450
  lpg = mq9.MQ9DataLPG();   // Gas licuado del petróleo (ppm)limite 450

  Serial.print("CO (ppm): ");
  Serial.println(co);
  
  Serial.print("CH4 (ppm): ");
  Serial.println(ch4);

  Serial.print("LPG (ppm): ");
  Serial.println(lpg);

  Serial.println("-----------------------");
}
void deserializeActuatorStatusBody(String responseJson)
{
  if (responseJson != "")
  {
    DynamicJsonDocument doc(2048);

    // Deserialize the JSON document
    DeserializationError error = deserializeJson(doc, responseJson);

    // Test if parsing succeeds.
    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // Fetch values.
    int idActuatorState = doc["idActuatorState"];
    float status = doc["status"];
    bool statusBinary = doc["statusBinary"];
    int idActuator = doc["idActuator"];
    long timestamp = doc["timestamp"];

    Serial.println(("Actuator status deserialized: [idActuatorState: " + String(idActuatorState) + ", status: " + String(status) + ", statusBinary: " + String(statusBinary) + ", idActuator" + String(idActuator) + ", timestamp: " + String(timestamp) + "]").c_str());
  }
}

void deserializeDeviceBody(int httpResponseCode)
{

  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String responseJson = http.getString();
    DynamicJsonDocument doc(2048);

    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    int idDevice = doc["idDevice"];
    String deviceSerialId = doc["deviceSerialId"];
    String name = doc["name"];
    String mqttChannel = doc["mqttChannel"];
    int idGroup = doc["idGroup"];

    Serial.println(("Device deserialized: [idDevice: " + String(idDevice) + ", name: " + name + ", deviceSerialId: " + deviceSerialId + ", mqttChannel" + mqttChannel + ", idGroup: " + idGroup + "]").c_str());
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void deserializeSensorsFromDevice(int httpResponseCode)
{

  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String responseJson = http.getString();
    // allocate the memory for the document
    DynamicJsonDocument doc(ESP.getMaxAllocHeap());

    // parse a JSON array
    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // extract the values
    JsonArray array = doc.as<JsonArray>();
    for (JsonObject sensor : array)
    {
      int idSensor = sensor["idSensor"];
      String name = sensor["name"];
      String sensorType = sensor["sensorType"];
      int idDevice = sensor["idDevice"];

      Serial.println(("Sensor deserialized: [idSensor: " + String(idSensor) + ", name: " + name + ", sensorType: " + sensorType + ", idDevice: " + String(idDevice) + "]").c_str());
    }
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void deserializeActuatorsFromDevice(int httpResponseCode)
{

  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String responseJson = http.getString();
    // allocate the memory for the document
    DynamicJsonDocument doc(ESP.getMaxAllocHeap());

    // parse a JSON array
    DeserializationError error = deserializeJson(doc, responseJson);

    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }

    // extract the values
    JsonArray array = doc.as<JsonArray>();
    for (JsonObject sensor : array)
    {
      int idActuator = sensor["idActuator"];
      String name = sensor["name"];
      String actuatorType = sensor["actuatorType"];
      int idDevice = sensor["idDevice"];

      Serial.println(("Actuator deserialized: [idActuator: " + String(idActuator) + ", name: " + name + ", actuatorType: " + actuatorType + ", idDevice: " + String(idDevice) + "]").c_str());
    }
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void test_response(int httpResponseCode)
{
  delay(test_delay);
  if (httpResponseCode > 0)
  {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
    String payload = http.getString();
    Serial.println(payload);
  }
  else
  {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }
}

void describe(char *description)
{
  if (describe_tests)
    Serial.println(description);
}

void GET_tests()
{
  describe("Test GET full device info");
  String serverPath = serverName + "api/devices/" + String(DEVICE_ID);
  http.begin(serverPath.c_str());
  // test_response(http.GET());
  deserializeDeviceBody(http.GET());

  describe("Test GET sensors from deviceID");
  serverPath = serverName + "api/devices/" + String(DEVICE_ID) + "/sensors";
  http.begin(serverPath.c_str());
  deserializeSensorsFromDevice(http.GET());

  describe("Test GET actuators from deviceID");
  serverPath = serverName + "api/devices/" + String(DEVICE_ID) + "/actuators";
  http.begin(serverPath.c_str());
  deserializeActuatorsFromDevice(http.GET());

  describe("Test GET sensors from deviceID and Type");
  serverPath = serverName + "api/devices/" + String(DEVICE_ID) + "/sensors/Temperature";
  http.begin(serverPath.c_str());
  deserializeSensorsFromDevice(http.GET());

  describe("Test GET actuators from deviceID");
  serverPath = serverName + "api/devices/" + String(DEVICE_ID) + "/actuators/Relay";
  http.begin(serverPath.c_str());
  deserializeActuatorsFromDevice(http.GET());
}

void POST_tests()
{
  String actuator_states_body = serializeActuatorStatusBody(random(2000, 4000) / 100, true, 1, millis());
  describe("Test POST with actuator state");
  String serverPath = serverName + "api/actuator_states";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_states_body));

 String sensor_value_body = serializeSensorValueBody(18, millis(), random(2000, 4000) / 100);
   describe("Test POST with sensor value");
  serverPath = serverName + "api/sensor_values";
  http.begin(serverPath.c_str());
  test_response(http.POST(sensor_value_body));

  // String device_body = serializeDeviceBody(String(DEVICE_ID), ("Name_" + String(DEVICE_ID)).c_str(), ("mqtt_" + String(DEVICE_ID)).c_str(), 12);
  // describe("Test POST with path and body and response");
  // serverPath = serverName + "api/device";
  // http.begin(serverPath.c_str());
  // test_response(http.POST(actuator_states_body));
}
void POST_sensores(String JSON)
{
  String actuator_states_body = JSON;
  describe("Post estado sensor");
  String serverPath = serverName + "/api/values";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_states_body));

  //String sensor_value_body = serializeSensorValueBody(18, millis(), random(2000, 4000) / 100);
  // describe("Test POST with sensor value");
  //serverPath = serverName + "api/sensor_values";
  //http.begin(serverPath.c_str());
  //test_response(http.POST(sensor_value_body));

  // String device_body = serializeDeviceBody(String(DEVICE_ID), ("Name_" + String(DEVICE_ID)).c_str(), ("mqtt_" + String(DEVICE_ID)).c_str(), 12);
  // describe("Test POST with path and body and response");
  // serverPath = serverName + "api/device";
  // http.begin(serverPath.c_str());
  // test_response(http.POST(actuator_states_body));
}
void POST_actuadores(String JSON)
{
  String actuator_states_body = JSON;
  describe("Post estado actuadores");
  String serverPath = serverName + "/api/state";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_states_body));

  //String sensor_value_body = serializeSensorValueBody(18, millis(), random(2000, 4000) / 100);
  // describe("Test POST with sensor value");
  //serverPath = serverName + "api/sensor_values";
  //http.begin(serverPath.c_str());
  //test_response(http.POST(sensor_value_body));

  // String device_body = serializeDeviceBody(String(DEVICE_ID), ("Name_" + String(DEVICE_ID)).c_str(), ("mqtt_" + String(DEVICE_ID)).c_str(), 12);
  // describe("Test POST with path and body and response");
  // serverPath = serverName + "api/device";
  // http.begin(serverPath.c_str());
  // test_response(http.POST(actuator_states_body));
}

// Run the tests!
void loop()
{
  //GET_tests();
  //POST_tests();
  HandleMqtt();
  leerMQ9();
  String valorCOMQ9= serializeSensorValueBody(0,1000000000,co);
  String valorCH4Q9= serializeSensorValueBody(1,10000000000,ch4);
  String valorLPGMQ9= serializeSensorValueBody(2,1000000000000,lpg);
  POST_sensores(valorCOMQ9);
  POST_sensores(valorCH4Q9);
  POST_sensores(valorLPGMQ9);
  Serial.println("Valor co");
  Serial.println(co);
  Serial.println("Valor lpg");
  Serial.println(lpg);
  Serial.println("Valor ch4");
  Serial.println(ch4);
  Serial.println(Ro);
  
}
