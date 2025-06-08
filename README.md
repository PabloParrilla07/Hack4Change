# Hack4Change
Proyectito para el hack4change
Cada vez que queráis importar el proyecto a eclipse, importar solo la carpeta "detectorGases", el resto no son cosas para eclipse.

# Detector de Gases – Proyecto IoT

## 📂 Estructura del Proyecto


# 🚀 Ejecución del Proyecto

- El archivo `MainVerticle` lanza ambos servidores REST.
- El **Servidor de Alto Nivel** se conecta al broker MQTT.
- El **Servidor de Bajo Nivel** gestiona directamente la base de datos.

## 🔁 Flujo General

## 📚 PARA MEMORIA

## 🗄️ Funciones de Base de Datos

- Imágenes de la base de datos (estructura y datos).
- Descripción de cada tabla.
- Verificación de funciones mediante pruebas.

## 📡 Lógica MQTT en el Servidor de Alto Nivel

- Subscripción a canales según grupos (`canalMQTT`).
- Publicación de estados y datos.
- Procesamiento de mensajes entrantes.

## 🔁 Comunicación con Servidor de Bajo Nivel

- Funciones de alto nivel que realizan peticiones `POST` al servidor de bajo nivel:
  - Añadir sensores/actuadores.
  - Actualizar valores.
  - Consultar estado de dispositivos.

## 🧪 MQTT Explorer

- Capturas de canales creados.
- Comprobación visual de:
  - Publicaciones desde ESP32.
  - Suscripciones desde el servidor.

---

## 🛠️ PlatformIO (Arduino)

### 📚 Bibliotecas Usadas
### 🔁 Lógica MQTT

- Conexión al broker MQTT.
- Suscripción a canal de control.
- Publicación periódica de datos del sensor.

### 📊 Sensores

- Lectura desde sensores analógicos/digitales.
- Conversión a JSON y envío.

### 🔁 Actuadores

- Cambio de estado mediante mensajes MQTT recibidos.

### 🌐 REST API en Arduino

- Envío de datos a servidores usando `HTTPClient`.
- Ejemplo de `POST` JSON a `/api/sensor-values`.

---

## 🔐 Acceso a la Base de Datos

- **Usuario**: `IoTAmaso`  
- **Contraseña**: `I0t34m4s0`

---

## 📸 Imágenes Recomendadas

- Estructura de la BDD.
- Capturas de MQTT Explorer.
- Ejecución del servidor.
- Comunicación con ESP32.


         
¡Aviso! MQTT lo usamos específicamente para trabajar con los actuadores y el uso de umbrales. La esp32 se susbribe al topic del actuador y esta recibe los datos.

