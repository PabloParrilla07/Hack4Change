package detectorGases;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import detectorGases.entidades.Sensor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class SensorClient extends AbstractVerticle{
	
	public SensorClientUtil SensorClientUtil;

	public void start(Promise<Void> startFuture) {
		WebClientOptions options = new WebClientOptions().setUserAgent("RestClientApp/2.0.2.1");
		options.setKeepAlive(false);
		SensorClientUtil = new SensorClientUtil(WebClient.create(vertx, options));

		/* --------------- GET many request --------------- */

		Promise<Sensor[]> resList = Promise.promise();
		resList.future().onComplete(complete -> {
			if (complete.succeeded()) {
				System.out.println("GetAll:");
				Stream.of(complete.result()).forEach(elem -> {
					System.out.println(elem.toString());
				});
			} else {
				System.out.println(complete.cause().toString());
			}
		});

		SensorClientUtil.getRequest(443, "https://67d2c5c590e0670699befc4a.mockapi.io", "api/v1/sensors", 
				Sensor[].class, resList);

		/* --------------- GET one request --------------- */

		Promise<Sensor> res = Promise.promise();
		res.future().onComplete(complete -> {
			if (complete.succeeded()) {
				System.out.println("GetOne");
				System.out.println(complete.result().toString());
			} else {
				System.out.println(complete.cause().toString());
			}
		});

	//	restClientUtil.getRequest(443, "https://67d2c5c590e0670699befc4a.mockapi.io", "api/v1/users/1", 
	//			UserEntity.class, res);
		SensorClientUtil.getRequest(443, "https://67d2c5c590e0670699befc4a.mockapi.io", "api/v1/sensors/2", 
				Sensor.class, res);

		/* --------------- GET request con parámetros--------------- */

//		Promise<Sensor> resWithParams = Promise.promise();
//		resWithParams.future().onComplete(complete -> {
//			if (complete.succeeded()) {
//				System.out.println("GetOne With params");
//				System.out.println(complete.result().toString());
//			} else {
//				System.out.println(complete.cause().toString());
//			}
//		});
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("name1", "type1");
//		params.put("name3", "type3");
//		params.put("name7", "type7");
//		SensorClientUtil.getRequestWithParams(443, "https://67d2c5c590e0670699befc4a.mockapi.io", "api/v1/users/1", 
//				Sensor.class,
//				resWithParams, params);

		/* --------------- POST request --------------- */

//		Promise<UserEntity> resPost = Promise.promise();
//		resPost.future().onComplete(complete -> {
//			if (complete.succeeded()) {
//				System.out.println("Post One");
//				System.out.println(complete.result().toString());
//			} else {
//				System.out.println(complete.cause().toString());
//			}
//		});
//
//		Calendar birthdate = Calendar.getInstance();
//		birthdate.set(Calendar.YEAR, 1985);
//		birthdate.set(Calendar.MONDAY, Calendar.JULY);
//		birthdate.set(Calendar.DAY_OF_MONTH, 4);
//
//		restClientUtil.postRequest(443, "https://67d2c5c590e0670699befc4a.mockapi.io", "api/v1/users",
//				new UserEntity(300, "Nuevo", "Usuario", birthdate.getTimeInMillis(), "nuevo_usuario", "pass"),
//				UserEntity.class, resPost);
//		
//		/* --------------- PUT request --------------- */
//
//		Promise<UserEntity> resPut = Promise.promise();
//		resPost.future().onComplete(complete -> {
//			if (complete.succeeded()) {
//				System.out.println("Put");
//				System.out.println(complete.result().toString());
//			} else {
//				System.out.println(complete.cause().toString());
//			}
//		});
//
//		Calendar birthdate2 = Calendar.getInstance();
//		birthdate2.set(Calendar.YEAR, 1985);
//		birthdate2.set(Calendar.MONDAY, Calendar.JULY);
//		birthdate2.set(Calendar.DAY_OF_MONTH, 4);
//
//		restClientUtil.putRequest(443, "https://67d2c5c590e0670699befc4a.mockapi.io", "api/v1/users/1",
//				new UserEntity(300, "Edito", "Usuario", birthdate.getTimeInMillis(), "nuevo_usuario", "pass"),
//				UserEntity.class, resPut);
//
//		/* --------------- REMOVE request --------------- */
//
//		Promise<String> resDelete = Promise.promise();
//		resDelete.future().onComplete(complete -> {
//			if (complete.succeeded()) {
//				System.out.println("Remove One");
//				System.out.println(complete.result().toString());
//			} else {
//				System.out.println(complete.cause().toString());
//			}
//		}).onFailure(fail -> {
//			System.out.println("Remove One");
//			System.out.println(fail.toString());
//		});
//
//		restClientUtil.deleteRequest(443, "https://67d2c5c590e0670699befc4a.mockapi.io", "api/v1/users/3",
//				resDelete);
//		
		
		/* --------------- LAUNCH local server --------------- */
		vertx.deployVerticle(SensorServer.class.getName(), deploy -> {
			if (deploy.succeeded()) {
				System.out.println("Verticle deployed");
			}else {
				System.out.println("Error deploying verticle");
			}
		});

	}

}
