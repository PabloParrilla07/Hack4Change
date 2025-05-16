package detectorGases.Rest;

import com.google.gson.Gson;
import detectorGases.entidades.SensorValue;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

public class RestHighServer extends AbstractVerticle{

	private Gson gson;
	private WebClient webClient;
	private MqttClient mqttClient;
	
	//NOMBRE DEL TOPIC
	String topic = "esp32/actuador/OLED";
	
	//FUNCIONES DEL RESTHIGHSERVER: -PUBLICA TOPICS
	//							    -ENVÍA DATOS AL SERVIDOR BAJO NIVEL
	//							    -CONTROL DE LOS ACTUADORES
	//								-RECIBE DATOS DE LA ESP32

	public void start(Promise<Void> startFuture) {
		
		gson = new Gson();
		
		webClient = WebClient.create(vertx, new WebClientOptions().setUserAgent("RestHighLevelClient"));
		
		//PARTE DE MQTT

		//CREAMOS EL CLIENTE MQTT
		mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		
		
		//LO CONECTAMOS AL BROKER
		mqttClient.connect(1883, "localhost", s -> {
			if(s.succeeded()) {
				System.out.println("Conectado al Broker MQTT");
			
			} else {
				System.out.println("No pudo conectarse al Broker MQTT!!");
			}
			
		});
		
		
		//AQUÍ RECIBE DE LA ESP32
		Router router = Router.router(vertx);
		router.route("/api/values*").handler(BodyHandler.create());
		//Qué significa esta línea?
		//Cuando la esp32 haga un POST, realizará la función que se esta declarando
		router.post("/api/values").handler(this::handleSensorValuePost);

		// Handling any server startup result
		vertx.createHttpServer().requestHandler(router::handle).listen(8080, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});

	}
	
	private void handleSensorValuePost(RoutingContext routingContext) {
		try {
			
			//CONVERTIMOS EL JSON QUE NOS DA LA ESP32 EN UN TIPO SENSOR VALUE
            SensorValue value = gson.fromJson(routingContext.getBodyAsString(), SensorValue.class);
            System.out.println("Valor del sensor: " + value);

            // USO DE LA LÓGICA(se que no es 500 es un valor predeterminado)
            if (value.getValue()> 500) {
                mqttClient.publish(topic, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                System.out.println("Pantalla ON");
            } else {
                mqttClient.publish(topic, Buffer.buffer("OFF"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                System.out.println("📡 Pantalla OFF");
            }

            // Reenviar el valor al servidor de bajo nivel
            webClient.post(8080, "localhost", "/api/values")
                    .sendBuffer(Buffer.buffer(gson.toJson(value)), res -> {
                        if (res.succeeded()) {
                        	routingContext.response().setStatusCode(201).end("Dato recibido y reenviado");
                        } else {
                        	routingContext.response().setStatusCode(500).end("Error reenviando a servidor bajo nivel");
                        }
                    });

        } catch (Exception e) {
        	routingContext.response().setStatusCode(400).end("JSON malformado");
        }
    }

	
}
