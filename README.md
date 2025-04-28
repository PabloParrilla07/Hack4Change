# Hack4Change
Proyectito para el hack4change
Cada vez que queráis importar el proyecto a eclipse, importar solo la carpeta "detectorGases", el resto no son cosas para eclipse.

To do list:
- API Rest
- Empezar a volcar codigo en la placa para probar sensores
- Hacer la Base de Datos
- Montaje(prioridad baja)
- Pruebas (**ultimo/menos importante**)
- Solucionar problemas con eclipse y git **prioritario rn**

Contraseña BDD:
usuario -> p4c0gu4p0

LO QUE HE ENTENDIDO DE LA API REST:
-ESTRUCTURA:
  detectorDeGases
  |--MainVerticle(Archivo)
  |--SensorVerticle(Archivo)
  |--SensorValueVerticle(Archivo)
  |-- ...
  |--entidades(Carpeta)
  |  |--Sensor
  |  |--SensorValue
  |  |--Actuador
  |  |--ActuadorState
  |  |--Grupo
  |  |--Dispositivo
  |--mqtt(Carpeta)
  |  |--AUN NO SE PERO MQTT
  |--rest(Carpeta)
     |--SensorServer
     |--SensorValueServer
     |--ActuadorServer
     |--ActuadorStateServer
     |--GrupoServer
     |--DispositivoServer

-EJECUCIÓN:
Según tengo entendido, en el MainVerticle desplegamos todos los verticles, y estos al mismo tiempo usan las funciones creadas en api rest(??).
Esto lo hacemos asi porque no es lo mismo crear datos en la base de datos de un Value que de un Sensor, me explico. En el value tenemos que estar haciendo
post contínuamente para tener los valores más actuales, en cambio, los sensor hacemos los posts una sola vez.
