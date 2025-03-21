package detectorGases;

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
	
	//TIMESTAMP DE TIPO LONG
	
	public SensorClientUtil(WebClient client) {
		gson = new Gson();
		this.client = client;
	}

	/**
	 * Get request utility
	 * 
	 * @param <T>       Type of result enveloped in JSON response
	 * @param port      Port
	 * @param host      Host address
	 * @param resource  URI where resource is provided
	 * @param classType Type of result enveloped in JSON response
	 * @param promise   Promise to be executed on call finish
	 */
	public <T> void getRequest(Integer port, String host, String resource, Class<T> classType, Promise<T> promise) {
		client.getAbs(host + ":" + port + "/" + resource).send(elem -> {
			if (elem.succeeded()) {
				promise.complete(gson.fromJson(elem.result().bodyAsString(), classType));
			} else {
				promise.fail(elem.cause());
			}
		});

	}

	/**
	 * Get request utility
	 * 
	 * @param <T>       Type of result enveloped in JSON response
	 * @param port      Port
	 * @param host      Host address
	 * @param resource  URI where resource is provided
	 * @param classType Type of result enveloped in JSON response
	 * @param promise   Promise to be executed on call finish
	 * @param params    Map with key-value entries for call parameters
	 */
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

	/**
	 * Post request utility
	 * 
	 * @param <B>       Type of body enveloped in JSON request
	 * @param <T>       Type of result enveloped in JSON response
	 * @param port      Port
	 * @param host      Host address
	 * @param resource  URI where resource is provided
	 * @param classType Type of result enveloped in JSON response
	 * @param promise   Promise to be executed on call finish
	 */
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

	/**
	 * Put request utility
	 * 
	 * @param <B>       Type of body enveloped in JSON request
	 * @param <T>       Type of result enveloped in JSON response
	 * @param port      Port
	 * @param host      Host address
	 * @param resource  URI where resource is provided
	 * @param classType Type of result enveloped in JSON response
	 * @param promise   Promise to be executed on call finish
	 */
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

	/**
	 * Delete request utility
	 * 
	 * @param port      Port
	 * @param host      Host address
	 * @param resource  URI where resource is provided
	 * @param promise   Promise to be executed on call finish
	 */
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
