# Hack4Change
Proyectito para el hack4change
Cada vez que queráis importar el proyecto a eclipse, importar solo la carpeta "detectorGases", el resto no son cosas para eclipse.

To do list:
- Finalizar lógica y conexiones de API REST.
- Empezar a volcar codigo en la placa para probar sensores
- Conectar la base de datos con el servidor de bajo nivel.
- Montaje(prioridad baja)
- Pruebas (**ultimo/menos importante**)

Contraseña BDD:
usuario -> IoTAmaso
contraseña -> I0t34m4s0

-ESTRUCTURA:
  detectorDeGases
  |--MainVerticle(Archivo)
  |--entidades(Carpeta)
  |  |--Sensor
  |  |--SensorValue
  |  |--Actuador
  |  |--ActuadorState
  |  |--Grupo
  |  |--Dispositivo
  |--rest(Carpeta)
     |--RestHighServer
     |--RestLowServer


-EJECUCIÓN:
El MainVerticle despliega los dos servidores Rest. El Servidor de alto nivel se conecta al broker del mqtt. El servidor de bajo nivel está preparado para gestionar datos

-FLUJO:
BASE DE DATOS <---> REST BAJO NIVEL <---> REST ALTO NIVEL <---> PLACA ESP32
         Mediante Funciones       Mediante          Mediante REST API
         y REST API               REST API               y MQTT
         
¡Aviso! MQTT lo usamos específicamente para trabajar con los actuadores y el uso de umbrales. La esp32 se susbribe al topic del actuador y esta recibe los datos.

-UMBRALES(El mq2, es el único que cuanto menor el valor peor)
SENSOR:             UMBRAL:
MQ-2	              RS/R0 < 2.0 → Gas, humo alto
MQ-9	              CO > 35 ppm
MICS5524	          Voltaje > 1.5V
PMS5003(PM2.5)	    PM2.5 > 25 µg/m³
PMS5003(PM10)	      PM10 > 50 µg/m³
MAX30105	          Valor > 800 (óptico)
