package detectorGases.Rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import detectorGases.entidades.Actuador;
import detectorGases.entidades.ActuadorState;
import detectorGases.entidades.Dispositivo;
import detectorGases.entidades.Grupo;
import detectorGases.entidades.Sensor;
import detectorGases.entidades.SensorValue;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public class RestLowServer extends AbstractVerticle{
	
	//Aquí van los métodos que usaremos para acceder/modificar la base de datos.
	
	
	//MAPA PARA CADA ENTIDAD CREADA. REUNIÓN DE DATOS
	private Map<Integer, Sensor> sensors = new HashMap<Integer, Sensor>();
	private Map<Integer, SensorValue> values = new HashMap<Integer, SensorValue>();
	private Map<Integer, Actuador> actuadores = new HashMap<Integer, Actuador>();
	private Map<Integer, ActuadorState> states = new HashMap<Integer, ActuadorState>();
	private Map<Integer, Grupo> groups = new HashMap<Integer, Grupo>();
	private Map<Integer, Dispositivo> devices = new HashMap<Integer, Dispositivo>();
	
	private Gson gson;

	public void start(Promise<Void> startFuture) {
		
		//CREACIÓN DE DATOS FICTICIOS
		createSomeSensors(9);
		createSomeValues(50);
		createSomeGroups(1);
		createSomeDevices(2);
		createSomeActuadors(1);
		createSomeActuadorStates(20);

		//FORMATO DE LA FECHA
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

		Router router = Router.router(vertx);

		// Handling any server startup result
		vertx.createHttpServer().requestHandler(router::handle).listen(8081, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});

		//SENSORES
		router.route("/api/sensors*").handler(BodyHandler.create());
		router.get("/api/sensors").handler(this::getAllSensors);
		router.get("/api/sensors/:id").handler(this::getOneSensor);
		router.post("/api/sensors").handler(this::addOneSensor);
		router.delete("/api/sensors/:id").handler(this::deleteOneSensor);
		router.put("/api/sensors/:id").handler(this::putOneSensor);
		
		//VALORES DEL SENSOR
		router.route("/api/values*").handler(BodyHandler.create());
		router.get("/api/values/:id_sensor").handler(this::getOneValue);
		//Este lo coge del servidor de alto nivel.
		router.post("/api/values").handler(this::addOneValue);
		
		//ACTUADORES
		router.route("/api/actuators*").handler(BodyHandler.create());
		router.get("/api/actuators").handler(this::getAllActuators);
		router.get("/api/actuators/:id").handler(this::getOneActuator);
		router.post("/api/actuators").handler(this::addOneActuator);
		router.delete("/api/actuators/:id").handler(this::deleteOneActuator);
		router.put("/api/actuators/:id").handler(this::putOneActuator);
		
		//ESTADO DEL ACTUADOR
		router.route("/api/states*").handler(BodyHandler.create());
		router.get("/api/states/:id_actuador").handler(this::getOneState);
		router.post("/api/states").handler(this::addOneState);
		
		//DISPOSITIVOS
		router.route("/api/devices*").handler(BodyHandler.create());
		router.get("/api/devices").handler(this::getAllDevices);
		router.get("/api/devices/:id").handler(this::getOneDevice);
		router.post("/api/devices").handler(this::addOneDevice);
		router.delete("/api/devices/:id").handler(this::deleteOneDevice);
		router.put("/api/devices/:id").handler(this::putOneDevice);
		
		//GRUPOS
		router.route("/api/groups*").handler(BodyHandler.create());
		router.get("/api/groups").handler(this::getAllGroups);
		router.get("/api/groups/:id").handler(this::getOneGroup);
		router.post("/api/groups").handler(this::addOneGroup);
		router.delete("/api/groups/:id").handler(this::deleteOneGroup);
		router.put("/api/groups/:id").handler(this::putOneGroup);
		
	}

	//SENSOR
	
	private void getAllSensors(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(sensors.values()));
	}

	private void getOneSensor(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("id"));
		if (sensors.containsKey(id)) {
			Sensor ds = sensors.get(id);
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
					.end(gson.toJson(ds));
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}

	private void addOneSensor(RoutingContext routingContext) {
		final Sensor sensor = gson.fromJson(routingContext.getBodyAsString(), Sensor.class);
		sensors.put(sensor.getIdentificador(), sensor);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(sensor));
	}

	private void deleteOneSensor(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("id"));
		if (sensors.containsKey(id)) {
			Sensor user = sensors.get(id);
			sensors.remove(id);
			routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
					.end(gson.toJson(user));
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end();
		}
	}
	
	//Put: Modificar objeto, no creas uno.
	private void putOneSensor(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("userid"));
		Sensor ds = sensors.get(id);
		final Sensor element = gson.fromJson(routingContext.getBodyAsString(), Sensor.class);
		ds.setNombre(element.getNombre());
		ds.setTipo(element.getTipo());
		ds.setDeviceID(element.getDeviceID());
		sensors.put(ds.getIdentificador(), ds);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(element));
	}
	
	
	//VALORES
	
	private void getOneValue(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("id_sensor"));
		if (sensors.containsKey(id)) {
			SensorValue ds = values.get(id);
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
					.end(gson.toJson(ds));
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}

	private void addOneValue(RoutingContext routingContext) {
		final SensorValue valor = gson.fromJson(routingContext.getBodyAsString(), SensorValue.class);
		values.put(valor.getId(), valor);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(valor));
	}
	
	//ACTUADORES
	
	private void getAllActuators(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(actuadores.values()));
	}

	private void getOneActuator(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("id"));
		if (actuadores.containsKey(id)) {
			Actuador ds = actuadores.get(id);
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
					.end(gson.toJson(ds));
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}

	private void addOneActuator(RoutingContext routingContext) {
		final Actuador act = gson.fromJson(routingContext.getBodyAsString(), Actuador.class);
		actuadores.put(act.getId(), act);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(act));
	}

	private void deleteOneActuator(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("id"));
		if (actuadores.containsKey(id)) {
			Actuador act = actuadores.get(id);
			actuadores.remove(id);
			routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
					.end(gson.toJson(act));
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end();
		}
	}
	
	private void putOneActuator(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("userid"));
		Actuador ds = actuadores.get(id);
		final Actuador element = gson.fromJson(routingContext.getBodyAsString(), Actuador.class);
		ds.setName(element.getName());
		ds.setType(element.getType());
		ds.setDeviceID(element.getDeviceID());
		actuadores.put(ds.getId(), ds);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(element));
	}
	
	//ESTADOS DE LOS ACTUADORES
	
	private void getOneState(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("id_sensor"));
		if (states.containsKey(id)) {
			ActuadorState ds = states.get(id);
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
					.end(gson.toJson(ds));
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}

	private void addOneState(RoutingContext routingContext) {
		final ActuadorState valor = gson.fromJson(routingContext.getBodyAsString(), ActuadorState.class);
		states.put(valor.getId(), valor);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(valor));
	}
	
	//DISPOSITIVOS
	
	private void getAllDevices(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(devices.values()));
	}

	private void getOneDevice(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("id"));
		if (devices.containsKey(id)) {
			Dispositivo ds = devices.get(id);
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
					.end(gson.toJson(ds));
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}

	private void addOneDevice(RoutingContext routingContext) {
		final Dispositivo device = gson.fromJson(routingContext.getBodyAsString(), Dispositivo.class);
		devices.put(device.getId(), device);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(device));
	}

	private void deleteOneDevice(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("id"));
		if (devices.containsKey(id)) {
			Dispositivo device = devices.get(id);
			devices.remove(id);
			routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
					.end(gson.toJson(device));
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end();
		}
	}
	
	private void putOneDevice(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("id"));
		Dispositivo ds = devices.get(id);
		final Dispositivo element = gson.fromJson(routingContext.getBodyAsString(), Dispositivo.class);
		ds.setNombre(element.getNombre());
		ds.setIdGrupo(element.getIdGrupo());
		devices.put(ds.getId(), ds);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(element));
	}
	
	
	//GRUPOS
	
	private void getAllGroups(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(groups.values()));
	}

	private void getOneGroup(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("id"));
		if (groups.containsKey(id)) {
			Grupo ds = groups.get(id);
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
					.end(gson.toJson(ds));
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}

	private void addOneGroup(RoutingContext routingContext) {
		final Grupo grupo = gson.fromJson(routingContext.getBodyAsString(), Grupo.class);
		groups.put(grupo.getId(), grupo);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(grupo));
	}

	private void deleteOneGroup(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("id"));
		if (groups.containsKey(id)) {
			Grupo grupo = groups.get(id);
			groups.remove(id);
			routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
					.end(gson.toJson(grupo));
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end();
		}
	}
	
	private void putOneGroup(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("id"));
		Grupo ds = groups.get(id);
		final Grupo element = gson.fromJson(routingContext.getBodyAsString(), Grupo.class);
		ds.setCanal_mqtt(element.getNombre());
		ds.setNombre(element.getNombre());
		groups.put(ds.getId(), ds);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(element));
	}
	
	
	//FUNCIONES DE CREACION DE DATOS
	
	private void createSomeSensors(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			sensors.put(id, new Sensor("Name" + id, "Type" + id, 0+id, 1+id));
		});
	}
	
	private void createSomeValues(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			values.put(id, new SensorValue(0 + id, 0 + id, (float)0+id, (long)0+id));
		});
	}
	
	private void createSomeGroups(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			groups.put(id, new Grupo(0 + id, "CanalMqtt" + id, "nombreGrupo" + id));
		});
	}
	
	private void createSomeDevices(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			devices.put(id, new Dispositivo(0 + id, "NombreDisp" + id, 0 + id));
		});
	}
	
	private void createSomeActuadors(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			actuadores.put(id, new Actuador(0 + id, "NombreActuador" + id, "TipoActuador" + id, 0 + id));
		});
	}
	
	private void createSomeActuadorStates(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			states.put(id, new ActuadorState(0 + id, 0 + id, Math.random() < 0.5, (long) 0 + id));
		});
	}
	
		

	
}
