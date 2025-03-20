package sensorVertx;

import java.util.Map;

import com.google.gson.Gson;

import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

public class SensorClientUtil {
	
	public WebClient client;
	private Gson gson;
	
	public SensorClientUtil(WebClient client) {
		gson = new Gson();
		this.client = client;
	}
	
	public <T> void getRequest(Integer port, String host, String resource, Class<T> classType, Promise<T> promise) {
		client.getAbs(host + ":" + port + "/" + resource).send(elem -> {
			if (elem.succeeded()) {
				promise.complete(gson.fromJson(elem.result().bodyAsString(), classType));
			} else {
				promise.fail(elem.cause());
			}
		});

	}
	
	public <T> void getRequestWithParams(Integer port, String host, String resource, Class<T> classType,
			Promise<T> promise, Map<String, String> params) {
		HttpRequest<Buffer> httpRequest = client.getAbs(host + ":" + port + "/" + resource);

		params.forEach((key, value) -> {
			httpRequest.addQueryParam(key, value);
		});

		httpRequest.send(elem -> {
			if (elem.succeeded()) {
				promise.complete(gson.fromJson(elem.result().bodyAsString(), classType));
			} else {
				promise.fail(elem.cause());
			}
		});

	}

	public <B, T> void postRequest(Integer port, String host, String resource, Object body, Class<T> classType,
			Promise<T> promise) {
		JsonObject jsonBody = new JsonObject(gson.toJson(body));
		client.postAbs(host + ":" + port + "/" + resource).sendJsonObject(jsonBody, elem -> {
			if (elem.succeeded()) {
				Gson gson = new Gson();
				promise.complete(gson.fromJson(elem.result().bodyAsString(), classType));
			} else {
				promise.fail(elem.cause());
			}
		});
	}

	public <B, T> void putRequest(Integer port, String host, String resource, Object body, Class<T> classType,
			Promise<T> promise) {
		JsonObject jsonBody = new JsonObject(gson.toJson(body));
		client.putAbs(host + ":" + port + "/" + resource).sendJsonObject(jsonBody, elem -> {
			if (elem.succeeded()) {
				Gson gson = new Gson();
				promise.complete(gson.fromJson(elem.result().bodyAsString(), classType));
			} else {
				promise.fail(elem.cause());
			}
		});
	}
	
	public void deleteRequest(Integer port, String host, String resource, Promise<String> promise) {
		client.deleteAbs(host + ":" + port + "/" + resource).send(elem -> {
			if (elem.succeeded()) {
				promise.complete(elem.result().bodyAsString());
			} else {
				promise.fail(elem.cause());
			}
		});

	}

}
