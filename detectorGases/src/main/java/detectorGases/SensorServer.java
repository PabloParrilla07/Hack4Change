package detectorGases;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class SensorServer extends AbstractVerticle{
	
	private Map<Integer, SensorEntities> sensors = new HashMap<Integer, SensorEntities>();
	private Gson gson;

	public void start(Promise<Void> startFuture) {
		// Creating some synthetic data
		createSomeData(25);

		// Instantiating a Gson serialize object using specific date format
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

		// Defining the router object
		Router router = Router.router(vertx);

		// Handling any server startup result
		vertx.createHttpServer().requestHandler(router::handle).listen(8080, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});

		// Defining URI paths for each method in RESTful interface, including body
		// handling by /api/users* or /api/users/*
		router.route("/api/sensors*").handler(BodyHandler.create());
		router.get("/api/sensors").handler(this::getAllWithParams);
		router.get("/api/users/:sensors").handler(this::getOne);
		router.post("/api/sensors").handler(this::addOne);
		router.delete("/api/sensors/:deviceID").handler(this::deleteOne);
		//router.put("/api/users/:userid").handler(this::putOne);
	}

	@SuppressWarnings("unused")
	private void getAll(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(sensors.values()));
	}

	private void getAllWithParams(RoutingContext routingContext) {
		final String tipo = routingContext.queryParams().contains("name") ? 
				routingContext.queryParam("type").get(0) : null;
		final Long timestamp = Long.valueOf(routingContext.queryParams().contains("surname") ? 
				routingContext.queryParam("timestamp").get(0) : null);
		final Float valor = Float.valueOf(routingContext.queryParams().contains("username") ? 
				routingContext.queryParam("value").get(0) : null);
		
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(sensors.values().stream().filter(elem -> {
					boolean res = true;
					res = res && tipo != null ? elem.getType().equals(tipo) : true;
					res = res && timestamp != null ? elem.getTimestamp()==timestamp : true;
					res = res && valor != null ? elem.getValue()==valor : true;
					return res;
				}).collect(Collectors.toList())));
	}

	private void getOne(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("deviceID"));
		if (sensors.containsKey(id)) {
			SensorEntities ds = sensors.get(id);
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
					.end(gson.toJson(ds));
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}

	private void addOne(RoutingContext routingContext) {
		final SensorEntities sensor = gson.fromJson(routingContext.getBodyAsString(), SensorEntities.class);
		sensors.put(sensor.deviceID, sensor);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(sensor));
	}

	private void deleteOne(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("deviceID"));
		if (sensors.containsKey(id)) {
			SensorEntities user = sensors.get(id);
			sensors.remove(id);
			routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
					.end(gson.toJson(user));
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end();
		}
	}
	
	//Put: Modificar objeto, no creas uno.
//	private void putOne(RoutingContext routingContext) {
//		int id = Integer.parseInt(routingContext.request().getParam("userid"));
//		UserEntity ds = users.get(id);
//		final UserEntity element = gson.fromJson(routingContext.getBodyAsString(), UserEntity.class);
//		ds.setName(element.getName());
//		ds.setSurname(element.getSurname());
//		ds.setBirthdate(element.getBirthdate());
//		ds.setPassword(element.getPassword());
//		ds.setUsername(element.getUsername());
//		users.put(ds.getIduser(), ds);
//		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
//				.end(gson.toJson(element));
//	}
	
	private void createSomeData(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			sensors.put(id, new SensorEntities(Calendar.getInstance().getTimeInMillis() + id, (float)100. + id,
					 "Type_" + id, id));
		});
	}
	

}
