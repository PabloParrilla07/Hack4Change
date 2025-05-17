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
	String act1 = "esp32/actuador/OLED";
	String act2 = "esp32/actuador/BOCINA";
	
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
		router.post("/api/values").handler(this::accionValuePost);

		// Handling any server startup result
		vertx.createHttpServer().requestHandler(router::handle).listen(8080, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});

	}
	
	private void accionValuePost(RoutingContext routingContext) {
		try {
			
			//CONVERTIMOS EL JSON QUE NOS DA LA ESP32 EN UN TIPO SENSOR VALUE
            SensorValue value = gson.fromJson(routingContext.getBodyAsString(), SensorValue.class);
            System.out.println("Valor del sensor: " + value);
            
            //Esto se puede ir cambiando, por ahora así.
            
            int mq2ID=0;
            int mq9ID=1;
            int micsID=2;
            int pmsID=3;
            int maxID=4;
            
            // USO DE LA LÓGICA
            //MQ2 --> < 2 
            //MQ9 --> >35
            //MICS --> >1.5V
            //PMS -->  >25 o 50 depende de cual usemos
            //MAX -->  >800
            
            
            if (value.getIdSensor().equals(mq2ID) && value.getValue()< 2) {
                mqttClient.publish(act1, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                mqttClient.publish(act2, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                System.out.println("Pantalla y Bocina ON");
            } 
            else if(value.getIdSensor().equals(mq9ID) && value.getValue()>35){
                mqttClient.publish(act1, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                mqttClient.publish(act2, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                System.out.println("Pantalla y Bocina ON");
            }
            else if(value.getIdSensor().equals(micsID) && value.getValue() > 1.5){
                mqttClient.publish(act1, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                mqttClient.publish(act2, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                System.out.println("Pantalla y Bocina ON");
            }
            else if(value.getIdSensor().equals(pmsID) && value.getValue() >50) {
                mqttClient.publish(act1, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                mqttClient.publish(act2, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                System.out.println("Pantalla y Bocina ON");
            }
            else if(value.getIdSensor().equals(maxID) && value.getValue() > 800){
                mqttClient.publish(act1, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                mqttClient.publish(act2, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                System.out.println("Pantalla y Bocina ON");
            }
            else {
                mqttClient.publish(act1, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                mqttClient.publish(act2, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                System.out.println("Pantalla y Bocina OFF");
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
