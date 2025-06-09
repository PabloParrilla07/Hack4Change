#include <HTTPClient.h>
#include "ArduinoJson.h"
#include <WiFiUdp.h>
#include <PubSubClient.h>
#include <MQSpaceData.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <Ethernet.h>
#include <MQUnifiedsensor.h>
#include "MAX30105.h"

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1
#define MQ9_PIN 35
#define IP "192.168.66.18"


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

MAX30105 particleSensor;
int ir;

char id;
String dato;

float LPG;
float CH4;
float CO;
int idSensorCH4=0;
int idSensorCO=0;
int idSensorLPG=0;
int idSensorMAX=0;

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


/// Lectura sensor MAX30105
void leerMAX()
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
}



// conecta o reconecta al MQTT
// consigue conectar -> suscribe a topic y publica un mensaje
// no -> espera 5 segundos


// gestiona la comunicación MQTT
// comprueba que el cliente está conectado
// no -> intenta reconectar
// si -> llama al MQTT loop

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
String response;
///Funciones de serialización de datos->json
String serializeSensorValueBody(int idSensor, long timestamp, float value)
{
  // StaticJsonObject allocates memory on the stack, it can be
  // replaced by DynamicJsonDocument which allocates in the heap.
  //
  DynamicJsonDocument doc(2048);

  // Add values in the document
  //
  if(idSensor==0){
    doc["sensorValueId"]=idSensorCH4;
    doc["sensorId"] = idSensor;
    doc["timestamp"] = timestamp;
    doc["value"] = value;
    idSensorCH4++;
  }else if(idSensor==1){
    doc["sensorValueId"]=idSensorCO;
    doc["sensorId"] = idSensor;
    doc["timestamp"] = timestamp;
    doc["value"] = value;
    idSensorCO++;
  }else if(idSensor==2){
    doc["sensorValueId"]=idSensorLPG;
    doc["sensorId"] = idSensor;
    doc["timestamp"] = timestamp;
    doc["value"] = value;
    idSensorLPG++;
  }else if(idSensor==3){
    doc["sensorValueId"]=idSensorMAX;
    doc["sensorId"] = idSensor;
    doc["timestamp"] = timestamp;
    doc["value"] = value;
    idSensorMAX++;
  }
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
String serializeSensorBody(int sensorId, String name, String type, int deviceId)
{
  DynamicJsonDocument doc(2048);

  doc["sensorId"] = sensorId;
  doc["name"] = name;
  doc["type"] = type;
  doc["deviceId"] = deviceId;

  String output;
  serializeJson(doc, output);
  return output;
}
String serializeDeviceBody(int dispositivoId, String name, int groupId)
{
  DynamicJsonDocument doc(2048);

  doc["dispositivoID"] = dispositivoId;
  doc["name"] = name;
  doc["groupId"] = groupId;
  String output;
  serializeJson(doc, output);
  return output;
}
String serializeGroup(int grupoId, String mqttChannel, String name)
{
  DynamicJsonDocument doc(2048);

  doc["grupoId"] = grupoId;
  doc["canal_mqtt"] = mqttChannel;
  doc["name"] = name;

  String output;
  serializeJson(doc, output);
  return output;
}

void deserializeActuatorStatusBody(String responseJson)
{
  if (responseJson != "")
  {
    DynamicJsonDocument doc(2048);

    // Deserialize the JSON document.
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
///Funciones de comunicación con el servidor mediante REST API
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
void POST_sensor(String JSON)
{
  String actuator_states_body = JSON;
  describe((char*)"Post sensor");
  String serverPath = serverName + "/api/sensors";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_states_body));
  http.end();
  delay(1000);
}void POST_grupos(String JSON)
{
  String grupo = JSON;
  describe((char*)"Post grupo");
  String serverPath = serverName + "/api/groups";
  http.begin(serverPath.c_str());
  test_response(http.POST(grupo));
  http.end();
  delay(1000);
}
void POST_sensores(String JSON)
{
  String actuator_states_body = JSON;
  describe((char*)"Post estado sensor");
  String serverPath = serverName + "/api/values";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_states_body));
  http.end();
  delay(1000);
}
void POST_actuadores(String JSON)
{
  String actuator_states_body = JSON;
  describe((char*)"Post estado actuadores");
  String serverPath = serverName + "/api/states";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_states_body));
}
void POST_device(String JSON)
{
  String device = JSON;
  describe((char*)"Post device");
  String serverPath = serverName + "/api/devices";
  http.begin(serverPath.c_str());
  test_response(http.POST(device));
  http.end();
  delay(1000);
}
// Setup
void setup()
{
  Serial.begin(9600);
  pinMode(12, OUTPUT);
  Serial.println("MAX30105 Basic Readings Example");
  
  
   //Initialize sensor
  if (particleSensor.begin() == false)
  {
    Serial.println("MAX30105 was not found. Please check wiring/power. ");
    while (1);
  }

  particleSensor.setup(); 
  //Configure sensor. Use 6.4mA for LED drive
  //onfiguración y calibración del MQ-9
  MQ9.setRegressionMethod(1); //_PPM =  a*ratio^b
 
  
  /*****************************  MQ Init ********************************************/ 
  ///configuración del MQ-9 usando librería MQUnifiedsensor
  MQ9.init(); 
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
  
  ///Conexión a WiFi  
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(STASSID);
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
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  Serial.println("Setup!");
  String jsonGrupo1= serializeGroup(0,"esp32/sensor/#","sensores");
  String jsonDispositivo=serializeDeviceBody(0,"sensores",0);
  String jsonSensor1=serializeSensorBody(0,"CH4","MQ-9",0);
  String jsonSensor2=serializeSensorBody(1,"CO","MQ-9",0);
  String jsonSensor3=serializeSensorBody(2,"LPG","MQ-9",0);
  String jsonSensor4=serializeSensorBody(3,"MAX30105","MAX30105",0);
  POST_grupos(jsonGrupo1);
  POST_device(jsonDispositivo);
  POST_sensor(jsonSensor1);
  POST_sensor(jsonSensor2);
  POST_sensor(jsonSensor3);
  POST_sensor(jsonSensor4);  
}
// Run the tests!
void loop()
{
  leerMQ9();
  leerMAX();
  String valorMAX= serializeSensorValueBody(3,millis(),ir);
  String valorCOMQ9= serializeSensorValueBody(0,millis(),CO);
  String valorCH4Q9= serializeSensorValueBody(1,millis(),CH4);
  String valorLPGMQ9= serializeSensorValueBody(2,millis(),LPG);
  POST_sensores(valorCOMQ9);
  POST_sensores(valorCH4Q9);
  POST_sensores(valorLPGMQ9);
  POST_sensores(valorMAX);
  Serial.println("Valor co");
  Serial.println(CO);
  Serial.println("Valor lpg");
  Serial.println(LPG);
  Serial.println("Valor ch4");
  Serial.println(CH4);
}
