#include <HTTPClient.h>
#include "ArduinoJson.h"
#include <WiFiUdp.h>
#include <PubSubClient.h>
#include <MQSpaceData.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <Ethernet.h>
#include <MQUnifiedsensor.h>
//#include "MAX30105.h"

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
#define MQ9_PIN 35
#define IP "192.168.66.18"
int bocinaActuatorId=0;
int pantallaActuatorId=0;
String dato2="";

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

#include <MQUnifiedsensor.h>
/************************Hardware Related Macros************************************/
#define         Board                   ("Arduino UNO")
#define         Pin                     (35)  //Analog input 4 of your arduino
/***********************Software Related Macros************************************/
#define         Type                    ("MQ-9") //MQ9
#define         Voltage_Resolution      (5)
#define         ADC_Bit_Resolution      (10) // For arduino UNO/MEGA/NANO
#define         RatioMQ9CleanAir        (9.6) //RS / R0 = 60 ppm 
/*****************************Globals***********************************************/
//Declare Sensor
MQUnifiedsensor MQ9(Board, Voltage_Resolution, ADC_Bit_Resolution, Pin, Type);

//MAX30105 particleSensor;
int ir;

char id;
String dato;

float LPG;
float CH4;
float CO;

MQSpaceData mq9(12, MQ9_PIN);
// Replace 0 by ID of this current device
const int DEVICE_ID = 0;

int test_delay = 1000; // so we don't spam the API
boolean describe_tests = true;

// Replace 0.0.0.0 by your server local IP (ipconfig [windows] or ifconfig [Linux o MacOS] gets IP assigned to your PC)
String serverName = "http://192.168.66.18:8080/";
HTTPClient http;

// Replace WifiName and WifiPassword by your WiFi credentials
#define STASSID "rafa_cuadrado"    //"Your_Wifi_SSID"
#define STAPSK "03102003RIcs" //"Your_Wifi_PASSWORD"

// MQTT configuration
WiFiClient espClient;
PubSubClient client(espClient);

// Server IP, where de MQTT broker is deployed
const char *MQTT_BROKER_ADRESS = IP;
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
  delay(2000);

}

void OnMqttReceived(char *topic, byte *payload, unsigned int length)
{
  Serial.print("Received on ");
  Serial.print(topic);
  String top = String(topic);
  Serial.print(": ");

  String content = "";
  for (size_t i = 0; i < length; i++)
  {
    content.concat((char)payload[i]);
  }

  id = content.charAt(0);  // El primer carácter indica el sensor ID
  dato = content.substring(1); // El resto es el dato a mostrar

  // Si el topic es el de OLED, muestra el mensaje en la pantalla OLED
  if (top == "esp32/actuador/OLED") {
    if (content != "connected") {
      Serial.println("Mensaje recibido en OLED");
      
      display.setTextSize(1);
      display.setTextColor(SSD1306_WHITE);
      if (id == '0') {
        display.setCursor(0, 0);
        display.fillRect(0, 0, 128, 8, SSD1306_BLACK);
        display.print(dato);
      } else if (id == '1') {
        display.setCursor(0, 8);
        display.fillRect(0, 8, 128, 8, SSD1306_BLACK);
        display.print(dato);
      } else if (id == '2') {
        display.setCursor(0, 16);
        display.fillRect(0, 16, 128, 8, SSD1306_BLACK);
        display.print(dato);
      } else if (id=='3') {
        display.setCursor(0,24);
        display.fillRect(0, 24, 128, 8, SSD1306_BLACK);
        display.print(dato);
      }

      display.display(); // Mostrar todo al final
    }
  }
  else if (top == "esp32/actuador/BOCINA") {
    if (content != "connected") {
      if (content == "ON") {
        Serial.println("Encendida");
        digitalWrite(12, HIGH);
        delay(2000);
        digitalWrite(12, LOW);
      } else {
        digitalWrite(12, LOW);
      }
      Serial.println("Mensaje recibido en BOCINA");
    }
  }
}

// inicia la comunicacion MQTT
// inicia establece el servidor y el callback al recibir un mensaje
void InitMqtt()
{
  client.setServer(MQTT_BROKER_ADRESS, MQTT_PORT);
  client.setCallback(OnMqttReceived);
}

/*void leerMAX()
{
  ir = particleSensor.getIR();
  if (ir > 10000) {
    Serial.println("¡Alta concentración de partículas!");
    Serial.println(ir);
  }else {
    Serial.println("Ambiente limpio.");
    Serial.println(ir);
  }
  delay(100);
}*/

// Setup
void setup()
{
  Serial.begin(9600);
  pinMode(12, OUTPUT);
    Serial.println("MAX30105 Basic Readings Example");

  // Initialize sensor
  //if (particleSensor.begin() == false)
 // {
  //  Serial.println("MAX30105 was not found. Please check wiring/power. ");
   // while (1);
 // }

  //particleSensor.setup(); 
  //Configure sensor. Use 6.4mA for LED drive
  // Configuración y calibración del MQ-9
  //MQ9.setRegressionMethod(1); //_PPM =  a*ratio^b
 
  
  /*****************************  MQ Init ********************************************/ 
  /*MQ9.init(); 
  Serial.print("Calibrating please wait.");
  float calcR0 = 0;
  for(int i = 1; i<=10; i ++)
  {
    MQ9.update(); // Update data, the arduino will read the voltage from the analog pin
    calcR0 += MQ9.calibrate(RatioMQ9CleanAir);
    Serial.print(".");
  }
  MQ9.setR0(calcR0/10);
  Serial.println("  done!.");
  
  if(isinf(calcR0)) {Serial.println("Warning: Conection issue, R0 is infinite (Open circuit detected) please check your wiring and supply"); while(1);}
  if(calcR0 == 0){Serial.println("Warning: Conection issue found, R0 is zero (Analog pin shorts to ground) please check your wiring and supply"); while(1);}
  */
  // Después de esto, puedes establecerlo manualmente si lo guardas
  
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(STASSID);
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
    client.subscribe("esp32/actuador/#");
    client.publish("esp32/actuador/OLED", "connected");
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

String serializeActuatorStatusBody(int idActuatorValue, bool statusBinary, int idActuator, long timestamp,String valor)
{
  DynamicJsonDocument doc(2048);

  doc["actuatorStateId"] = idActuatorValue;
  doc["idActuator"] = idActuator;
  doc["statusBinary"] = statusBinary;
  doc["valor"]= valor;
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

  MQ9.update();

  MQ9.setA(1000.5); MQ9.setB(-2.186); // Configure the equation to to calculate LPG concentration
  LPG = MQ9.readSensor(); // Sensor will read PPM concentration using the model, a and b values set previously or from the setup

  MQ9.setA(4269.6); MQ9.setB(-2.648); // Configure the equation to to calculate LPG concentration
  CH4 = MQ9.readSensor(); // Sensor will read PPM concentration using the model, a and b values set previously or from the setup

  MQ9.setA(599.65); MQ9.setB(-2.244); // Configure the equation to to calculate LPG concentration
  CO = MQ9.readSensor(); // Sensor will read PPM concentration using the model, a and b values set previously or from the setup

  Serial.print("CO (ppm): ");
  Serial.println(CO);
  
  Serial.print("CH4 (ppm): ");
  Serial.println(CH4);

  Serial.print("LPG (ppm): ");
  Serial.println(LPG);

  Serial.println("-----------------------");
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
  String actuator_states_body = serializeActuatorStatusBody(random(2000, 4000) / 100, true, 1, millis(),"");
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
  String serverPath = serverName + "/api/states";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_states_body));

  //String sensor_value_body = serializeSensorValueBody(18, millis(), random(2000, 4000) / 100);
  //("Test POST with sensor value");
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
  //leerMQ9();
  //leerMAX();
 
  //POST_sensores(valorCOMQ9);
  //POST_sensores(valorCH4Q9);
  //POST_sensores(valorLPGMQ9);
  //POST_sensores(valorMAX);
  String yeison=serializeActuatorStatusBody(pantallaActuatorId, true,0,100000,dato2);
  POST_actuadores(yeison);
  pantallaActuatorId++;
}
