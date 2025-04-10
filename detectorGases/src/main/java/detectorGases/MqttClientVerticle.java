package detectorGases;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import detectorGases.entidades.Sensor;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

public class MqttClientVerticle extends AbstractVerticle {
	
	Gson gson;

	public void start(Promise<Void> startFuture) {
		gson = new Gson();
		MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
		mqttClient.connect(1883, "192.168.184.42", s -> {

			mqttClient.subscribe("topic_2", MqttQoS.AT_LEAST_ONCE.value(), handler -> {
				if (handler.succeeded()) {
					System.out.println("Suscripción " + mqttClient.clientId());
				}
			});

			mqttClient.publishHandler(handler -> {
				System.out.println("Mensaje recibido:");
				System.out.println("    Topic: " + handler.topicName().toString());
				System.out.println("    Id del mensaje: " + handler.messageId());
				System.out.println("    Contenido: " + handler.payload().toString());
				try {
				Sensor sc = gson.fromJson(handler.payload().toString(), Sensor.class);
				System.out.println("    SimpleClass: " + sc.toString());
				}catch (JsonSyntaxException e) {
					System.out.println("    No es una SimpleClass. ");
				}
			});
			mqttClient.publish("topic_6", Buffer.buffer("Rico mango"), MqttQoS.AT_LEAST_ONCE, false, false);
		});

	}

}
