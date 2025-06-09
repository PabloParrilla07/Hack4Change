#include <HTTPClient.h>
#include "ArduinoJson.h"
#include <WiFiUdp.h>
#include <PubSubClient.h>
#include <MQSpaceData.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <Ethernet.h>
#include <MQUnifiedsensor.h>

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


char id;
String dato;

float LPG;
float CH4;
float CO;
int actuatorStateID=0;
String datoCo;
String datoLPG;
String datoCH4;
String datoMax;
String datoBocina="Apagada";
boolean bocinaState = false;

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
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.println("Pantalla lista");
  display.display();
  delay(3000);
  display.clearDisplay();

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
        datoCH4 = dato;
        display.print(dato);
        
      } else if (id == '1') {
        display.setCursor(0, 8);
        display.fillRect(0, 8, 128, 8, SSD1306_BLACK);
        datoCo = dato;
        display.print(dato);
        
      } else if (id == '2') {
        display.setCursor(0, 16);
        display.fillRect(0, 16, 128, 8, SSD1306_BLACK);
        datoLPG = dato;
        display.print(dato);
        
      } else if (id=='3') {
        display.setCursor(0,24);
        display.fillRect(0, 24, 128, 8, SSD1306_BLACK);
        datoMax= dato;
        display.print(dato);
        
      }
      display.display();
    }
  
  }
  else if (top == "esp32/actuador/BOCINA") {
    if (content != "connected") {
      if (content == "ON") {
        Serial.println("Encendida");
        bocinaState = true;
        digitalWrite(12, HIGH);
        delay(2000);
        digitalWrite(12, LOW);
      } else {
        digitalWrite(12, LOW);
        bocinaState = false;
        
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

// Setup


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
///Funciones de serialización de datos->json
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

String serializeActuatorStatusBody(int idActuatorValue, bool statusBinary, int idActuator, long timestamp, String valor)
{
  DynamicJsonDocument doc(2048);

  // Nombres de campos exactamente iguales a la clase Java
  doc["actuadorStateId"] = idActuatorValue;
  doc["actuadorId"] = idActuator;
  doc["state"] = statusBinary;
  doc["valor"] = valor;
  doc["timestamp"] = timestamp;

  String output;
  serializeJson(doc, output);
  return output;
}
String serializeDeviceBody(int dispositivoId, String name, int groupId)
{
  DynamicJsonDocument doc(2048);

  doc["dispositivoId"] = dispositivoId;
  doc["name"] = name;
  doc["grupoId"] = groupId;
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
String serializeActuatorBody(int actuadorId, String name, String type, int dispositivoId)
{
  DynamicJsonDocument doc(2048);

  doc["actuadorId"] = actuadorId;
  doc["name"] = name;
  doc["type"] = type;
  doc["dispositivoId"] = dispositivoId;

  String output;
  serializeJson(doc, output);
  return output;
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
void POST_sensores(String JSON)
{
  String actuator_states_body = JSON;
  describe((char*)"Post estado sensor");
  String serverPath = serverName + "/api/values";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_states_body));
}
void POST_actuadores(String JSON)
{
  String actuator_states_body = JSON;
  describe((char*)"Post estado actuadores");
  String serverPath = serverName + "/api/states";
  http.begin(serverPath.c_str());
  test_response(http.POST(actuator_states_body));
}
void POST_grupos(String JSON)
{
  String grupo = JSON;
  describe((char*)"Post grupo");
  String serverPath = serverName + "/api/groups";
  http.begin(serverPath.c_str());
  test_response(http.POST(grupo));
  http.end();
  delay(1000);
}
void POST_actuador(String JSON)
{
  String actuator_states_body = JSON;
  describe((char*)"Post estado actuadores");
  String serverPath = serverName + "/api/actuators";
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
void setup()
{
  Serial.begin(9600);
  pinMode(12, OUTPUT);
    Serial.println("MAX30105 Basic Readings Example");

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
  String jsonGrupo1= serializeGroup(1,"esp32/actuador/#","actuador");
  String jsonDispositivo2=serializeDeviceBody(1,"actuadores",1);
  String jsonActuador1=serializeActuatorBody(0,"OLED","OLED",1);
  String jsonActuador2=serializeActuatorBody(1,"BOCINA","BOCINA",1);
  
  POST_grupos(jsonGrupo1);
  POST_device(jsonDispositivo2);
  POST_actuador(jsonActuador1);
  POST_actuador(jsonActuador2);
  
  
}
// Run the tests!
void loop()
{
  HandleMqtt();
  String yeison=serializeActuatorStatusBody(actuatorStateID, true,0,millis(),datoCH4+" "+datoCo+" "+datoLPG+" "+datoMax);
  POST_actuadores(yeison);
  actuatorStateID++;
  if(bocinaState==true){
    datoBocina="encendida";
  }else{
    datoBocina="apagada";
  }
  String yeison2=serializeActuatorStatusBody(actuatorStateID,bocinaState,1,millis(),datoBocina);
  POST_actuadores(yeison2);
  actuatorStateID++;
}
