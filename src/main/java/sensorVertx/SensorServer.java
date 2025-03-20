package sensorVertx;

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
	
	private Map<Integer,Sensors> sensors = new HashMap<>();
	private Gson gson;

	public void start(Promise<Void> startFuture) {
		createSomeData(25);
		
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
		Router router = Router.router(vertx);
		
		vertx.createHttpServer().requestHandler(router::handle).listen(8080, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});
		
		router.route("/api/sensors*").handler(BodyHandler.create());
		router.get("/api/sensors").handler(this::getAllWithParams);
		router.get("/api/sensors/:deviceID").handler(this::getOne);
		router.post("/api/sensors").handler(this::addOne);
		router.delete("/api/sensors/:deviceID").handler(this::deleteOne);
//		router.put("/api/sensors/:deviceID").handler(this::putOne);
		
	}
	
	@SuppressWarnings("unused")
	private void getAll(RoutingContext routingContext) {
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(sensors.values()));
	}
	
	private void getAllWithParams(RoutingContext routingContext) {
		final String tipo = routingContext.queryParams().contains("type") ? 
				routingContext.queryParam("type").get(0) : null;
		final Float valor = Float.parseFloat(routingContext.queryParams().contains("value") ? 
				routingContext.queryParam("value").get(0) : null);
		final Long timestamp = Long.parseLong(routingContext.queryParams().contains("timestamp") ? 
				routingContext.queryParam("timestamp").get(0) : null);
		
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
				.end(gson.toJson(sensors.values().stream().filter(elem -> {
					boolean res = true;
					res = res && tipo != null ? elem.getType().equals(tipo) : true;
					res = res && valor != null ? elem.getValue() == valor : true;
					res = res && timestamp != null ? elem.getTimestamp() == timestamp : true;
					return res;
				}).collect(Collectors.toList())));
	}
	
	private void getOne(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("deviceID"));
		if (sensors.containsKey(id)) {
			Sensors ds = sensors.get(id);
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(200)
					.end(gson.toJson(ds));
		} else {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(204)
					.end();
		}
	}
	
	private void addOne(RoutingContext routingContext) {
		final Sensors user = gson.fromJson(routingContext.getBodyAsString(), Sensors.class);
		sensors.put(user.getDeviceID(), user);
		routingContext.response().setStatusCode(201).putHeader("content-type", "application/json; charset=utf-8")
				.end(gson.toJson(user));
	}
	
	private void deleteOne(RoutingContext routingContext) {
		int id = Integer.parseInt(routingContext.request().getParam("deviceID"));
		if (sensors.containsKey(id)) {
			Sensors user = sensors.get(id);
			sensors.remove(id);
			routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
					.end(gson.toJson(user));
		} else {
			routingContext.response().setStatusCode(204).putHeader("content-type", "application/json; charset=utf-8")
					.end();
		}
	}
	
	private void createSomeData(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			sensors.put(id, new Sensors(Calendar.getInstance().getTimeInMillis(), (float)0.+id,
					 "Tipo_"+id, id));
		});
	}

}
