package detectorGases.Rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

public class RestLowServer extends AbstractVerticle{
	
	//Aquí van los métodos que usaremos para acceder/modificar la base de datos.
	MySQLPool mySqlClient;
	
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
		router.get("/api/sensors/:id").handler(this::getSensorbyId);
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
		router.get("/api/actuators/:id").handler(this::getActuatorById);
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
		router.get("/api/devices/:id").handler(this::getDeviceById);
		router.post("/api/devices").handler(this::addOneDevice);
		router.delete("/api/devices/:id").handler(this::deleteOneDevice);
		router.put("/api/devices/:id").handler(this::putOneDevice);
		
		//GRUPOS
		router.route("/api/groups*").handler(BodyHandler.create());
		router.get("/api/groups").handler(this::getAllGroups);
		router.get("/api/groups/:id").handler(this::getGroupById);
		router.post("/api/groups").handler(this::addOneGroup);
		router.delete("/api/groups/:id").handler(this::deleteOneGroup);
		router.put("/api/groups/:id").handler(this::putOneGroup);
		
	}

	//SENSOR
	
	private void getAllSensors(RoutingContext routingContext) {
	    mySqlClient
	        .preparedQuery("SELECT * FROM Sensor;")
	        .execute(ar -> {
	            if (ar.succeeded()) {
	                RowSet<Row> resultSet = ar.result();
	                JsonArray result = new JsonArray();
	                for (Row row : resultSet) {
	                    result.add(JsonObject.mapFrom(new Sensor(
	                    		row.getString("type"),
								row.getString("name"),
								row.getInteger("sensorId"),
								row.getInteger("dispositivoId")
	                    )));
	                }

	                if (result.isEmpty()) {
	                    routingContext.response()
	                        .putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(404)
	                        .end("No se encontraron sensores");
	                } else {
	                    routingContext.response()
	                        .putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(200)
	                        .end(result.encode());
	                }
	            } else {
	                routingContext.response()
	                    .putHeader("content-type", "application/json; charset=utf-8")
	                    .setStatusCode(500)
	                    .end("Error al ejecutar la consulta: " + ar.cause().getMessage());
	            }
	        });
	}

/*	private void getOneSensor(RoutingContext routingContext) {
	    mySqlClient
        .preparedQuery("SELECT * FROM Sensor LIMIT 1;")
        .execute(ar -> {
            if (ar.succeeded()) {
                RowSet<Row> resultSet = ar.result();
                JsonArray result = new JsonArray();
                for (Row row : resultSet) {
                    result.add(JsonObject.mapFrom(new Sensor(
                    		row.getString("type"),
							row.getString("name"),
							row.getInteger("sensorId"),
							row.getInteger("dispositivoId")
                    )));
                }

                if (result.isEmpty()) {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(404)
                        .end("No se encontraron sensores");
                } else {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(result.encode());
                }
            } else {
                routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end("Error al ejecutar la consulta: " + ar.cause().getMessage());
            }
        });
	} */

	private void addOneSensor(RoutingContext routingContext) {
		final Sensor aux = gson.fromJson(routingContext.getBodyAsString(), Sensor.class);
		Integer idSensor = aux.getIdentificador();
		Integer idPlaca = aux.getDeviceID();
		String tipoSensor = aux.getTipo();

		if (idSensor == null || idPlaca == null || tipoSensor == null) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
			.setStatusCode(400).end("Campos requeridos (NOT NULL):\n\tidSensor\n\tidPlaca\n\tvalor1\n\ttipoSensor\n\ttiempo");
			return;
		}

		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery(
						"INSERT INTO Sensores(sensorId, deviceId, tipo,) VALUES (?, ?, ?)").execute(
						Tuple.of(idSensor, idPlaca,tipoSensor),
						res -> {
							if (res.succeeded()) {
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(201).end("Sensor añadido");
							} else {
								routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
								.setStatusCode(500).end("Error al añadir: " + res.cause().getLocalizedMessage());
							}
							connection.result().close();
						});
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(500).end("Error al conectar: " + connection.cause().toString());
			}
		});
	}

	private void deleteOneSensor(RoutingContext routingContext) {
		 int id = 0;
		    try {
		        id = Integer.parseInt(routingContext.request().getParam("id"));
		        final int sensorId = id;

		        mySqlClient.getConnection(conn -> {
		            if (conn.succeeded()) {
		                SqlConnection connection = conn.result();
		                connection
		                    .preparedQuery("DELETE FROM Sensor WHERE sensorId = ? LIMIT 1;")
		                    .execute(Tuple.of(sensorId), ar -> {
		                        if (ar.succeeded()) {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(200)
		                                .end("{\"message\":\"Sensor eliminado correctamente\"}");
		                        } else {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(500)
		                                .end("Error al ejecutar la eliminación: " + ar.cause().toString());
		                        }
		                        connection.close(); // Liberar la conexión
		                    });
		            } else {
		                routingContext.response()
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .setStatusCode(500)
		                    .end("Error al conectar: " + conn.cause().toString());
		            }
		        });
		    } catch (NumberFormatException e) {
		        routingContext.response()
		            .putHeader("content-type", "application/json; charset=utf-8")
		            .setStatusCode(400)
		            .end("Error: ID inválido - " + e.getLocalizedMessage());
		    }
	}
	
	//Put: Modificar objeto, no creas uno.
	private void putOneSensor(RoutingContext routingContext) {
		 JsonObject body = routingContext.getBodyAsJson();

		    if (body == null || !body.containsKey("id") || !body.containsKey("name")
		        || !body.containsKey("type") || !body.containsKey("dispositivoId")) {
		        routingContext.response()
		            .putHeader("content-type", "application/json; charset=utf-8")
		            .setStatusCode(400)
		            .end("Error: Faltan parámetros en el cuerpo de la petición.");
		        return;
		    }

		    int id = body.getInteger("id");
		    String name = body.getString("name");
		    String type = body.getString("type");
		    int dispositivoId = body.getInteger("dispositivoId");

		    mySqlClient
		        .preparedQuery("UPDATE Sensor SET name = ?, type = ?, dispositivoId = ? WHERE sensorId = ?;")
		        .execute(Tuple.of(name, type, dispositivoId, id), ar -> {
		            if (ar.succeeded()) {
		                if (ar.result().rowCount() == 0) {
		                    routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(404)
		                        .end("No se encontró el sensor con id: " + id);
		                } else {
		                    routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(200)
		                        .end("{\"message\":\"Sensor actualizado correctamente\"}");
		                }
		            } else {
		                routingContext.response()
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .setStatusCode(500)
		                    .end("Error al ejecutar la actualización: " + ar.cause().getMessage());
		            }
		        });
	}
	
	private void getSensorbyId(RoutingContext routingContext){
		int id = 0;
		try {
			id = Integer.parseInt(routingContext.request().getParam("id"));
			final int comp = id;
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery("SELECT * FROM Sensores WHERE idSensor = ? LIMIT 1;").execute(
							Tuple.of(comp),
							res -> {
								if (res.succeeded()) {
									// Get the result set
									RowSet<Row> resultSet = res.result();
									JsonArray result = new JsonArray();
									for (Row row : resultSet) {
										result.add(JsonObject.mapFrom(new Sensor(
												row.getString("type"),
												row.getString("name"),
												row.getInteger("sensorId"),
												row.getInteger("dispositivoId"))));
									}
									if (result.isEmpty()) {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(404).end("No se encontraron sensores");
									} else {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
									}
								} else {
									routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
									.setStatusCode(500).end("Error: " + res.cause().getLocalizedMessage());
								}
								connection.result().close();
							});
				} else {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.setStatusCode(500).end("Error al conectar: " + connection.cause().toString());
				}
			});
		} catch (NumberFormatException e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
			.end("Error: " + e.getLocalizedMessage());
		}

	}
	
	
	//VALORES
	
	private void getOneValue(RoutingContext routingContext) {
		mySqlClient
        .preparedQuery("SELECT * FROM SensorValue LIMIT 1;")
        .execute(ar -> {
            if (ar.succeeded()) {
                RowSet<Row> resultSet = ar.result();
                JsonArray result = new JsonArray();
                for (Row row : resultSet) {
                    result.add(JsonObject.mapFrom(new SensorValue(
                    		row.getInteger("sensorValueId"),
							row.getInteger("sensorId"),
							row.getFloat("value"),
							row.getLong("timestamp")
							)));
                }

                if (result.isEmpty()) {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(404)
                        .end("No se encontraron valroes");
                } else {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(result.encode());
                }
            } else {
                routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end("Error al ejecutar la consulta: " + ar.cause().getMessage());
            }
        });
	}

	private void addOneValue(RoutingContext routingContext) {
		final SensorValue aux = gson.fromJson(routingContext.getBodyAsString(), SensorValue.class);
		Integer idSensorValue = aux.getId();
		Float value = aux.getValue();
		Long timestamp = aux.getTimestamp();
		Integer sensorId = aux.getIdSensor();

		if (idSensorValue == null || value == null || timestamp == null || sensorId == null) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
			.setStatusCode(400).end("Campos requeridos (NOT NULL):\n\tidSensorValue\n\tvalor\n\ttimestamp\n\tsensorId");
			return;
		}

		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery(
					"INSERT INTO SensorValue(sensorValueId, value, timestamp, sensorId) VALUES (?, ?, ?, ?);")
					.execute(Tuple.of(idSensorValue, value, timestamp, sensorId), res -> {
						if (res.succeeded()) {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(201).end("Valor del sensor añadido");
						} else {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(500).end("Error al añadir: " + res.cause().getLocalizedMessage());
						}
						connection.result().close();
					});
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(500).end("Error al conectar: " + connection.cause().toString());
			}
		});
	}
	
	//ACTUADORES
	
	private void getAllActuators(RoutingContext routingContext) {
		mySqlClient
        .preparedQuery("SELECT * FROM Actuador;")
        .execute(ar -> {
            if (ar.succeeded()) {
                RowSet<Row> resultSet = ar.result();
                JsonArray result = new JsonArray();
                for (Row row : resultSet) {
                    result.add(JsonObject.mapFrom(new Actuador(
                    		row.getInteger("actuadorId"),
							row.getString("name"),
							row.getString("type"),
							row.getInteger("dispositivoId"))));
                }

                if (result.isEmpty()) {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(404)
                        .end("No se encontraron actuadores");
                } else {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(result.encode());
                }
            } else {
                routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end("Error al ejecutar la consulta: " + ar.cause().getMessage());
            }
        });
	}

/*	private void getOneActuator(RoutingContext routingContext) {
		mySqlClient
        .preparedQuery("SELECT * FROM Actuador LIMIT 1;")
        .execute(ar -> {
            if (ar.succeeded()) {
                RowSet<Row> resultSet = ar.result();
                JsonArray result = new JsonArray();
                for (Row row : resultSet) {
                    result.add(JsonObject.mapFrom(new Actuador(
                    		row.getInteger("actuadorId"),
							row.getString("name"),
							row.getString("type"),
							row.getInteger("dispositivoId"))));
                }

                if (result.isEmpty()) {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(404)
                        .end("No se encontraron actuadores");
                } else {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(result.encode());
                }
            } else {
                routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end("Error al ejecutar la consulta: " + ar.cause().getMessage());
            }
        });
	}*/

	private void addOneActuator(RoutingContext routingContext) {
		final Actuador aux = gson.fromJson(routingContext.getBodyAsString(), Actuador.class);
		Integer idActuador = aux.getId();
		Integer idPlaca = aux.getDeviceID();
		String tipoActuador = aux.getType();
		String nombre = aux.getName();

		if (idActuador == null || idPlaca == null || tipoActuador == null || nombre == null) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
			.setStatusCode(400).end("Campos requeridos (NOT NULL):\n\tidActuador\n\tidPlaca\n\ttipoActuador\n\tnombre");
			return;
		}

		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery(
					"INSERT INTO Actuador(actuadorId, dispositivoId, type, name) VALUES (?, ?, ?, ?);")
					.execute(Tuple.of(idActuador, idPlaca, tipoActuador, nombre), res -> {
						if (res.succeeded()) {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(201).end("Actuador añadido");
						} else {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(500).end("Error al añadir: " + res.cause().getLocalizedMessage());
						}
						connection.result().close();
					});
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(500).end("Error al conectar: " + connection.cause().toString());
			}
		});
	}

	private void deleteOneActuator(RoutingContext routingContext) {
		 int id = 0;
		    try {
		        id = Integer.parseInt(routingContext.request().getParam("id"));
		        final int sensorId = id;

		        mySqlClient.getConnection(conn -> {
		            if (conn.succeeded()) {
		                SqlConnection connection = conn.result();
		                connection
		                    .preparedQuery("DELETE FROM Actuador WHERE actuadorId = ? LIMIT 1;")
		                    .execute(Tuple.of(sensorId), ar -> {
		                        if (ar.succeeded()) {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(200)
		                                .end("{\"message\":\"Actuador eliminado correctamente\"}");
		                        } else {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(500)
		                                .end("Error al ejecutar la eliminación: " + ar.cause().toString());
		                        }
		                        connection.close(); // Liberar la conexión
		                    });
		            } else {
		                routingContext.response()
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .setStatusCode(500)
		                    .end("Error al conectar: " + conn.cause().toString());
		            }
		        });
		    } catch (NumberFormatException e) {
		        routingContext.response()
		            .putHeader("content-type", "application/json; charset=utf-8")
		            .setStatusCode(400)
		            .end("Error: ID inválido - " + e.getLocalizedMessage());
		    }
	}
	
	private void putOneActuator(RoutingContext routingContext) {
		 JsonObject body = routingContext.getBodyAsJson();

		    if (body == null || !body.containsKey("id") || !body.containsKey("name")
		        || !body.containsKey("type") || !body.containsKey("dispositivoId")) {
		        routingContext.response()
		            .putHeader("content-type", "application/json; charset=utf-8")
		            .setStatusCode(400)
		            .end("Error: Faltan parámetros en el cuerpo de la petición.");
		        return;
		    }

		    int id = body.getInteger("id");
		    String name = body.getString("name");
		    String type = body.getString("type");
		    int dispositivoId = body.getInteger("dispositivoId");

		    mySqlClient
		        .preparedQuery("UPDATE Actuador SET name = ?, type = ?, dispositivoId = ? WHERE actuadorId = ?;")
		        .execute(Tuple.of(name, type, dispositivoId, id), ar -> {
		            if (ar.succeeded()) {
		                if (ar.result().rowCount() == 0) {
		                    routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(404)
		                        .end("No se encontró el actuador con id: " + id);
		                } else {
		                    routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(200)
		                        .end("{\"message\":\"Actuador actualizado correctamente\"}");
		                }
		            } else {
		                routingContext.response()
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .setStatusCode(500)
		                    .end("Error al ejecutar la actualización: " + ar.cause().getMessage());
		            }
		        });
	}
	
	private void getActuatorById(RoutingContext routingContext) {
		int id = 0;
		try {
			id = Integer.parseInt(routingContext.request().getParam("id"));
			final int comp = id;
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery(
							"SELECT * FROM Actuadores WHERE idActuador = ?;").execute(
							Tuple.of(comp),
							res -> {
								if (res.succeeded()) {
									// Get the result set
									RowSet<Row> resultSet = res.result();
									JsonArray result = new JsonArray();
									for (Row row : resultSet) {
										result.add(JsonObject.mapFrom(new Actuador(
												row.getInteger("actuadorId"),
												row.getString("name"),
												row.getString("type"),
												row.getInteger("dispositivoId"))));
									}
									if (result.isEmpty()) {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(404).end("No se encontraron actuadores");
									} else {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
									}
								} else {
									routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
									.setStatusCode(500).end("Error: " + res.cause().getLocalizedMessage());
								}
								connection.result().close();
							});
				} else {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.setStatusCode(500).end("Error al conectar: " + connection.cause().toString());
				}
			});
		} catch (NumberFormatException e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
			.end("Error: " + e.getLocalizedMessage());
		}
	}
	
	//ESTADOS DE LOS ACTUADORES
	
	private void getOneState(RoutingContext routingContext) {
		mySqlClient
        .preparedQuery("SELECT * FROM ActuadorState LIMT 1;")
        .execute(ar -> {
            if (ar.succeeded()) {
                RowSet<Row> resultSet = ar.result();
                JsonArray result = new JsonArray();
                for (Row row : resultSet) {
                    result.add(JsonObject.mapFrom(new ActuadorState(
                    		row.getInteger("actuadorStateId"),
							row.getInteger("actuadorId"),
							row.getBoolean("state"),
							row.getLong("timestamp")
							)));
                }

                if (result.isEmpty()) {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(404)
                        .end("No se encontraron estados");
                } else {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(result.encode());
                }
            } else {
                routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end("Error al ejecutar la consulta: " + ar.cause().getMessage());
            }
        });
	}

	private void addOneState(RoutingContext routingContext) {
		final ActuadorState aux = gson.fromJson(routingContext.getBodyAsString(), ActuadorState.class);
		Integer idEstado = aux.getId();
		Boolean estado = aux.getEstado();
		Long timestamp = aux.getTimestamp();
		Integer actuadorId = aux.getId_actuador();

		if (idEstado == null || estado == null || timestamp == null || actuadorId == null) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
			.setStatusCode(400).end("Campos requeridos (NOT NULL):\n\tidEstado\n\testado\n\ttimestamp\n\tactuadorId");
			return;
		}

		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery(
					"INSERT INTO ActuadorState(actuadorStateId, state, timestamp, actuadorId) VALUES (?, ?, ?, ?);")
					.execute(Tuple.of(idEstado, estado, timestamp, actuadorId), res -> {
						if (res.succeeded()) {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(201).end("Estado del actuador añadido");
						} else {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(500).end("Error al añadir: " + res.cause().getLocalizedMessage());
						}
						connection.result().close();
					});
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(500).end("Error al conectar: " + connection.cause().toString());
			}
		});
	}
	
	//DISPOSITIVOS
	
	private void getAllDevices(RoutingContext routingContext) {
		mySqlClient
        .preparedQuery("SELECT * FROM Dispositivo;")
        .execute(ar -> {
            if (ar.succeeded()) {
                RowSet<Row> resultSet = ar.result();
                JsonArray result = new JsonArray();
                for (Row row : resultSet) {
                    result.add(JsonObject.mapFrom(new Dispositivo(
                    		row.getInteger("dispositivoId"),
							row.getString("name"),
							row.getInteger("grupoId")
							)));
                }

                if (result.isEmpty()) {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(404)
                        .end("No se encontraron dispositivos");
                } else {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(result.encode());
                }
            } else {
                routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end("Error al ejecutar la consulta: " + ar.cause().getMessage());
            }
        });
	}
	
	private void getDeviceById(RoutingContext routingContext) {
		int id = 0;
		try {
			id = Integer.parseInt(routingContext.request().getParam("id"));
			final int comp = id;
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery(
							"SELECT * FROM Placas WHERE idPlaca = ?;").execute(
							Tuple.of(comp),
							res -> {
								if (res.succeeded()) {
									// Get the result set
									RowSet<Row> resultSet = res.result();
									JsonArray result = new JsonArray();
									for (Row elem : resultSet) {
										result.add(JsonObject.mapFrom(new Dispositivo(
												elem.getInteger("dispositivoId"),
												elem.getString("name"),
												elem.getInteger("grupoId"))));
									}
									if (result.isEmpty()) {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(404).end("No se encontraron placas");
									} else {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
									}
								} else {
									routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
									.setStatusCode(500).end("Error: " + res.cause().getLocalizedMessage());
								}
								connection.result().close();
							});
				} else {
					routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
					.setStatusCode(500).end("Error al conectar: " + connection.cause().toString());
				}
			});
		} catch (NumberFormatException e) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8").setStatusCode(400)
			.end("Error: " + e.getLocalizedMessage());
		}
	}

/*	private void getOneDevice(RoutingContext routingContext) {
		mySqlClient
        .preparedQuery("SELECT * FROM Dispositivo LIMIT 1;")
        .execute(ar -> {
            if (ar.succeeded()) {
                RowSet<Row> resultSet = ar.result();
                JsonArray result = new JsonArray();
                for (Row row : resultSet) {
                    result.add(JsonObject.mapFrom(new Dispositivo(
                    		row.getInteger("dispositivoId"),
							row.getString("name"),
							row.getInteger("grupoId")
							)));
                }

                if (result.isEmpty()) {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(404)
                        .end("No se encontraron dispositivos");
                } else {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(result.encode());
                }
            } else {
                routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end("Error al ejecutar la consulta: " + ar.cause().getMessage());
            }
        });
	}*/

	private void addOneDevice(RoutingContext routingContext) {
		final Dispositivo aux = gson.fromJson(routingContext.getBodyAsString(), Dispositivo.class);
		Integer idDispositivo = aux.getId();
		String nombre = aux.getNombre();
		Integer grupoId = aux.getIdGrupo();

		if (idDispositivo == null || nombre == null || grupoId == null) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
			.setStatusCode(400).end("Campos requeridos (NOT NULL):\n\tidDispositivo\n\tnombre\n\tgrupoId");
			return;
		}

		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery(
					"INSERT INTO Dispositivo(dispositivoId, name, grupoId) VALUES (?, ?, ?);")
					.execute(Tuple.of(idDispositivo, nombre, grupoId), res -> {
						if (res.succeeded()) {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(201).end("Dispositivo añadido");
						} else {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(500).end("Error al añadir: " + res.cause().getLocalizedMessage());
						}
						connection.result().close();
					});
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(500).end("Error al conectar: " + connection.cause().toString());
			}
		});
	}

	private void deleteOneDevice(RoutingContext routingContext) {
		 int id = 0;
		    try {
		        id = Integer.parseInt(routingContext.request().getParam("id"));
		        final int sensorId = id;

		        mySqlClient.getConnection(conn -> {
		            if (conn.succeeded()) {
		                SqlConnection connection = conn.result();
		                connection
		                    .preparedQuery("DELETE FROM Dispositivo WHERE dispositivoId = ? LIMIT 1;")
		                    .execute(Tuple.of(sensorId), ar -> {
		                        if (ar.succeeded()) {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(200)
		                                .end("{\"message\":\"Dispositivo eliminado correctamente\"}");
		                        } else {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(500)
		                                .end("Error al ejecutar la eliminación: " + ar.cause().toString());
		                        }
		                        connection.close(); // Liberar la conexión
		                    });
		            } else {
		                routingContext.response()
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .setStatusCode(500)
		                    .end("Error al conectar: " + conn.cause().toString());
		            }
		        });
		    } catch (NumberFormatException e) {
		        routingContext.response()
		            .putHeader("content-type", "application/json; charset=utf-8")
		            .setStatusCode(400)
		            .end("Error: ID inválido - " + e.getLocalizedMessage());
		    }
	}
	
	private void putOneDevice(RoutingContext routingContext) {
		JsonObject body = routingContext.getBodyAsJson();

	    if (body == null || !body.containsKey("id") || !body.containsKey("name") || !body.containsKey("grupoId")) {
	        routingContext.response()
	            .putHeader("content-type", "application/json; charset=utf-8")
	            .setStatusCode(400)
	            .end("Error: Faltan parámetros en el cuerpo de la petición.");
	        return;
	    }

	    int id = body.getInteger("id");
	    String name = body.getString("name");
	    int grupoId = body.getInteger("grupoId");

	    mySqlClient
	        .preparedQuery("UPDATE Dispositivo SET name = ?, grupoId = ? WHERE dispositivoId = ?;")
	        .execute(Tuple.of(name, grupoId, id), ar -> {
	            if (ar.succeeded()) {
	                if (ar.result().rowCount() == 0) {
	                    routingContext.response()
	                        .putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(404)
	                        .end("No se encontró el dispositivo con id: " + id);
	                } else {
	                    routingContext.response()
	                        .putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(200)
	                        .end("{\"message\":\"Dispositivo actualizado correctamente\"}");
	                }
	            } else {
	                routingContext.response()
	                    .putHeader("content-type", "application/json; charset=utf-8")
	                    .setStatusCode(500)
	                    .end("Error al ejecutar la actualización: " + ar.cause().getMessage());
	            }
	        });
	}
	
	
	//GRUPOS
	
	private void getAllGroups(RoutingContext routingContext) {
		mySqlClient
        .preparedQuery("SELECT * FROM Grupo;")
        .execute(ar -> {
            if (ar.succeeded()) {
                RowSet<Row> resultSet = ar.result();
                JsonArray result = new JsonArray();
                for (Row row : resultSet) {
                    result.add(JsonObject.mapFrom(new Grupo(
                    		row.getInteger("grupoId"),
							row.getString("canalMQTT"),
							row.getString("name")
							)));
                }

                if (result.isEmpty()) {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(404)
                        .end("No se encontraron grupos");
                } else {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(result.encode());
                }
            } else {
                routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end("Error al ejecutar la consulta: " + ar.cause().getMessage());
            }
        });
	}

/*	private void getOneGroup(RoutingContext routingContext) {
		mySqlClient
        .preparedQuery("SELECT * FROM Grupo LIMIT 1;")
        .execute(ar -> {
            if (ar.succeeded()) {
                RowSet<Row> resultSet = ar.result();
                JsonArray result = new JsonArray();
                for (Row row : resultSet) {
                    result.add(JsonObject.mapFrom(new Grupo(
                    		row.getInteger("grupoId"),
							row.getString("canalMQTT"),
							row.getString("name")
							)));
                }

                if (result.isEmpty()) {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(404)
                        .end("No se encontraron grupos");
                } else {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(result.encode());
                }
            } else {
                routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end("Error al ejecutar la consulta: " + ar.cause().getMessage());
            }
        });
	}*/

	private void getGroupById(RoutingContext routingContext) {
		int id = 0;
		try {
			id = Integer.parseInt(routingContext.request().getParam("id"));
			final int grupoId = id;

			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery(
							"SELECT * FROM Grupo WHERE grupoId = ?;")
						.execute(Tuple.of(grupoId), res -> {
							if (res.succeeded()) {
								RowSet<Row> resultSet = res.result();
								JsonArray result = new JsonArray();
								for (Row elem : resultSet) {
									result.add(JsonObject.mapFrom(new Grupo(
										elem.getInteger("grupoId"),
										elem.getString("name"),
										elem.getString("canalMQTT")
									)));
								}
								if (result.isEmpty()) {
									routingContext.response()
										.putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(404).end("No se encontró ningún grupo con ese id");
								} else {
									routingContext.response()
										.putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
								}
							} else {
								routingContext.response()
									.putHeader("content-type", "application/json; charset=utf-8")
									.setStatusCode(500).end("Error: " + res.cause().getLocalizedMessage());
							}
							connection.result().close();
						});
				} else {
					routingContext.response()
						.putHeader("content-type", "application/json; charset=utf-8")
						.setStatusCode(500).end("Error al conectar: " + connection.cause().toString());
				}
			});
		} catch (NumberFormatException e) {
			routingContext.response()
				.putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(400).end("Error: " + e.getLocalizedMessage());
		}
	}
	
	private void addOneGroup(RoutingContext routingContext) {
		final Grupo aux = gson.fromJson(routingContext.getBodyAsString(), Grupo.class);
		Integer idGrupo = aux.getId();
		String nombre = aux.getNombre();
		String canalMQTT = aux.getCanal_mqtt();

		if (idGrupo == null || nombre == null || canalMQTT == null) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
			.setStatusCode(400).end("Campos requeridos (NOT NULL):\n\tidGrupo\n\tnombre\n\tcanalMQTT");
			return;
		}

		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery(
					"INSERT INTO Grupo(grupoId, name, canalMQTT) VALUES (?, ?, ?);")
					.execute(Tuple.of(idGrupo, nombre, canalMQTT), res -> {
						if (res.succeeded()) {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(201).end("Grupo añadido");
						} else {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(500).end("Error al añadir: " + res.cause().getLocalizedMessage());
						}
						connection.result().close();
					});
			} else {
				routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.setStatusCode(500).end("Error al conectar: " + connection.cause().toString());
			}
		});
	}

	private void deleteOneGroup(RoutingContext routingContext) {
		 int id = 0;
		    try {
		        id = Integer.parseInt(routingContext.request().getParam("id"));
		        final int sensorId = id;

		        mySqlClient.getConnection(conn -> {
		            if (conn.succeeded()) {
		                SqlConnection connection = conn.result();
		                connection
		                    .preparedQuery("DELETE FROM Grupo WHERE grupoId = ? LIMIT 1;")
		                    .execute(Tuple.of(sensorId), ar -> {
		                        if (ar.succeeded()) {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(200)
		                                .end("{\"message\":\"Grupo eliminado correctamente\"}");
		                        } else {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(500)
		                                .end("Error al ejecutar la eliminación: " + ar.cause().toString());
		                        }
		                        connection.close(); // Liberar la conexión
		                    });
		            } else {
		                routingContext.response()
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .setStatusCode(500)
		                    .end("Error al conectar: " + conn.cause().toString());
		            }
		        });
		    } catch (NumberFormatException e) {
		        routingContext.response()
		            .putHeader("content-type", "application/json; charset=utf-8")
		            .setStatusCode(400)
		            .end("Error: ID inválido - " + e.getLocalizedMessage());
		    }
	}
	
	private void putOneGroup(RoutingContext routingContext) {
		 JsonObject body = routingContext.getBodyAsJson();

		    if (body == null || !body.containsKey("id") || !body.containsKey("name") || !body.containsKey("canalMQTT")) {
		        routingContext.response()
		            .putHeader("content-type", "application/json; charset=utf-8")
		            .setStatusCode(400)
		            .end("Error: Faltan parámetros en el cuerpo de la petición.");
		        return;
		    }

		    int id = body.getInteger("id");
		    String name = body.getString("name");
		    String canalMQTT = body.getString("canalMQTT");

		    mySqlClient
		        .preparedQuery("UPDATE Grupo SET name = ?, canalMQTT = ? WHERE grupoId = ?;")
		        .execute(Tuple.of(name, canalMQTT, id), ar -> {
		            if (ar.succeeded()) {
		                if (ar.result().rowCount() == 0) {
		                    routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(404)
		                        .end("No se encontró el grupo con id: " + id);
		                } else {
		                    routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(200)
		                        .end("{\"message\":\"Grupo actualizado correctamente\"}");
		                }
		            } else {
		                routingContext.response()
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .setStatusCode(500)
		                    .end("Error al ejecutar la actualización: " + ar.cause().getMessage());
		            }
		        });
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
