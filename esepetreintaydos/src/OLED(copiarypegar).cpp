#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define OLED_RESET -1

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

//INICIALIZACIÓN
//ESTA FUNCIÓN METELA DONDE QUIERAS, QUE NO MOLESTE
void initOLED() {
  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println(F("Fallo en OLED"));
    while (true);
  }
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.println("Pantalla lista");
  //YO AQUÍ HARÍA UN DELAY DE TRES SEGUNDOS
  //Y LUEGO BORRARÍA ESE MENSAJE PARA TENER EL ESPACIO
  display.display();
}

//ESTO IRÍA DENTRO DEL SETUP DEL MAIN.CPP, NO HACE FALTA CREAR OTRO SETUP
void setup() {
  Serial.begin(115200);
  initOLED();
}

//ESTO IRÍA DONDE SE LEE EL TOPIC DEL ACTUADOR EN MQTT "OnMqttReceived"
//Y LA PANTALLA IMPRIMRÍA LO QUE LEE.
display.clearDisplay();
display.setCursor(0, 0);
display.setTextSize(1);
display.setTextColor(SSD1306_WHITE);
display.println("Gas data:");
display.println(content);
display.display();
