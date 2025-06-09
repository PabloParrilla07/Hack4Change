package detectorGases.Rest;

import com.google.gson.Gson;

import detectorGases.entidades.Actuador;
import detectorGases.entidades.ActuadorState;
import detectorGases.entidades.Dispositivo;
import detectorGases.entidades.Grupo;
import detectorGases.entidades.Sensor;
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
	
	String IP = "192.168.66.18";
	
    Float mq9CH4Value;
    Float mq9C0Value;
    Float mq9GLPValue;
    Float maxValue;
	
    Integer ID;
    
    
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
		mqttClient.connect(1883, IP, s -> {
			if(s.succeeded()) {
				System.out.println("Conectado al Broker MQTT");
			
			} else {
				System.out.println("No pudo conectarse al Broker MQTT!!");
			}
			
		});
		
		
		//ESTOS ENDPOINTS LOS HAREMOS PARA REENVIAR LA INFORMACIÓN AL SERVIDOR BAJO NIVEL
		
		//AQUÍ RECIBE DE LA ESP32
		Router router = Router.router(vertx);
		router.route("/api/groups*").handler(BodyHandler.create());
		router.route("/api/devices*").handler(BodyHandler.create());
		router.route("/api/sensors*").handler(BodyHandler.create());
		router.route("/api/actuators*").handler(BodyHandler.create());
		router.route("/api/values*").handler(BodyHandler.create());
		router.route("/api/states*").handler(BodyHandler.create());
		
		router.post("/api/groups").handler(this::accionGroupPost);
		router.post("/api/devices").handler(this::accionDevicePost);
		router.post("/api/sensors").handler(this::accionSensorPost);
		router.post("/api/actuators").handler(this::accionActuatorPost);
		router.post("/api/values").handler(this::accionValuePost);
		router.post("/api/states").handler(this::accionStatePost);

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
            
            int mq9CH4=0;
            int mq9C0=1;
            int mq9GLP=2;
            int MAX = 3;
            
            String oledMessage = "";
            
            ID = value.getIdSensor();
            
            //Sensores realmente
             
            //IDEAS PARA LA OLED:
            	//HAGO UNA SOLA LÍNEA EN ECLIPSE, DIVIDO EN VISUAL STUDIO
            	//HAGO VARIAS LÍNEAS EN ECLIPSE, EN VISUAL STUDIO NO SE COMO SE HARÍA
            	//CREO MÁS TOPICS PARA SUSCRIBIRSE Y COMPROBAR LA ID DEL SENSOR
            
            // USO DE LA LÓGICA
            //MQ2 --> < 2 
            //MQ9 --> >35
            //MICS --> >1.5V
            //PMS -->  >25 o 50 depende de cual usemos
            //MAX -->  >800
            
            if (ID.equals(mq9CH4)) {
                mq9CH4Value = value.getValue();
                oledMessage = ID + "CH4: " + mq9CH4Value;
            }
            if (ID.equals(mq9C0))  {
                mq9C0Value = value.getValue();
                oledMessage = ID + "CO: " + mq9C0Value;
            }
            if (ID.equals(mq9GLP)) {
                mq9GLPValue = value.getValue();
                oledMessage = ID + "GLP: " + mq9GLPValue;
            }
            if(ID.equals(MAX)) {
            	maxValue = value.getValue();
            	oledMessage = ID+ "Particulas: " + maxValue;
            }


            mqttClient.publish(act1, Buffer.buffer(oledMessage),MqttQoS.AT_LEAST_ONCE, false, false);


            
            
            if(ID.equals(mq9CH4) && value.getValue()>2000){
                mqttClient.publish(act2, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                System.out.println("Bocina ON");
            }
            else if(ID.equals(mq9C0) && value.getValue() > 450){
                mqttClient.publish(act2, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                System.out.println("Bocina ON");
            }
            else if(ID.equals(mq9GLP) && value.getValue() > 450){
                mqttClient.publish(act2, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                System.out.println("Bocina ON");
            }
            else if(ID.equals(MAX) && value.getValue() > 10000){
                mqttClient.publish(act2, Buffer.buffer("ON"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                System.out.println("Bocina ON");
            }
            else {
                mqttClient.publish(act2, Buffer.buffer("OFF"),
                        MqttQoS.AT_LEAST_ONCE, false, false);
                System.out.println("Bocina OFF");
            }
            
            System.out.println("Mensaje OLED publicado:\n" + oledMessage);
            
            // Reenviar el valor al servidor de bajo nivel
            webClient.post(8081, IP, "/api/values")
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

	private void accionGroupPost(RoutingContext routingContext) {
		try {
			
			Grupo group = gson.fromJson(routingContext.getBodyAsString(), Grupo.class);
			System.out.println("Grupo: " + group);
			webClient.post(8081, IP, "/api/groups")
            .sendBuffer(Buffer.buffer(gson.toJson(group)), res -> {
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
	
	private void accionDevicePost(RoutingContext routingContext) {
		try {
			
			Dispositivo device = gson.fromJson(routingContext.getBodyAsString(), Dispositivo.class);
			System.out.println("Dispositivo: " + device);
			webClient.post(8081, IP, "/api/devices")
            .sendBuffer(Buffer.buffer(gson.toJson(device)), res -> {
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
	
	private void accionSensorPost(RoutingContext routingContext) {
		try {
			
			Sensor sensor = gson.fromJson(routingContext.getBodyAsString(), Sensor.class);
			System.out.println("Sensor: " + sensor);
			webClient.post(8081, IP, "/api/sensors")
            .sendBuffer(Buffer.buffer(gson.toJson(sensor)), res -> {
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
	
	private void accionActuatorPost(RoutingContext routingContext) {
		try {
			
			Actuador actuator = gson.fromJson(routingContext.getBodyAsString(), Actuador.class);
			System.out.println("Actuador: " + actuator);
			webClient.post(8081, IP, "/api/actuators")
            .sendBuffer(Buffer.buffer(gson.toJson(actuator)), res -> {
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
	
	private void accionStatePost(RoutingContext routingContext) {
		try {
			
			ActuadorState state = gson.fromJson(routingContext.getBodyAsString(), ActuadorState.class);
			System.out.println("Valor del actuador: " + state);
			webClient.post(8081, IP, "/api/states")
            .sendBuffer(Buffer.buffer(gson.toJson(state)), res -> {
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
