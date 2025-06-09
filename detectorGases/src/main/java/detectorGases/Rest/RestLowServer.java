package detectorGases.Rest;

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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

public class RestLowServer extends AbstractVerticle{
	
	//CREACIÓN DEL CLIENTE DE MYSQL
	MySQLPool mySqlClient;
	
	//REUNIÓN DE DATOS FICTICIOS PARA PRUEBAS
//	private Map<Integer, Sensor> sensors = new HashMap<Integer, Sensor>();
//	private Map<Integer, SensorValue> values = new HashMap<Integer, SensorValue>();
//	private Map<Integer, Actuador> actuadores = new HashMap<Integer, Actuador>();
//	private Map<Integer, ActuadorState> states = new HashMap<Integer, ActuadorState>();
//	private Map<Integer, Grupo> groups = new HashMap<Integer, Grupo>();
//	private Map<Integer, Dispositivo> devices = new HashMap<Integer, Dispositivo>();
	
	private Gson gson;	
	
	public void start(Promise<Void> startFuture) {
		
		//CREACIÓN DE DATOS FICTICIOS
//		createSomeSensors(9);
//		createSomeValues(50);
//		createSomeGroups(1);
//		createSomeDevices(2);
//		createSomeActuadors(1);
//		createSomeActuadorStates(20);

		//GSON PARA LA TRADUCCIÓN DE LA FECHA
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

		//ROUTER DEL VERTX PARA HANDLERS
		Router router = Router.router(vertx);

		//CREACIÓN DEL SERVIDOR, ASIGNÁNDOLE EL PUERTO
		vertx.createHttpServer().requestHandler(router::handle).listen(8081, result -> {
			if (result.succeeded()) {
				startFuture.complete();
			} else {
				startFuture.fail(result.cause());
			}
		});

		//CONEXIÓN CON LA BASE DE DATOS
		MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(3306).setHost("127.0.0.1")
				.setDatabase("BDD_DAD").setUser("IoTAmaso").setPassword("I0t34m4s0");
		
		PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

		mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);
		
		//SENSORES
		router.route("/api/sensors*").handler(BodyHandler.create());
		router.get("/api/sensors").handler(this::getAllSensors);
		router.get("/api/sensors/:id").handler(this::getSensorbyId);
		
		
		router.post("/api/sensors").handler(this::addOneSensor);
		
		
		router.delete("/api/sensors/:id").handler(this::deleteOneSensor);
		router.put("/api/sensors/:id").handler(this::putOneSensor);
		
		//VALORES DEL SENSOR
		router.route("/api/values*").handler(BodyHandler.create());
		router.get("/api/values/:id_sensor").handler(this::getValuesbySensorId);
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
		router.get("/api/states/:id_actuador").handler(this::getStatesbyActuatorId);
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
	
	//OBTENER TODOS LOS SENSORES
	private void getAllSensors(RoutingContext routingContext) {
	    mySqlClient
	        .preparedQuery("SELECT * FROM Sensor;")
	        .execute(ar -> {
	            if (ar.succeeded()) {
	                RowSet<Row> resultSet = ar.result();
	                JsonArray result = new JsonArray();
	                for (Row row : resultSet) {
	                    result.add(JsonObject.mapFrom(new Sensor(
	                    		row.getInteger("idSensor"),
	                    		row.getString("nombre"),
	                    		row.getString("tipo"),
								row.getInteger("idDispositivo")
	                    )));
	                }

	                if (result.isEmpty()) {
	                    routingContext.response()
	                        .putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(404)
	                        .end("No se encontraron sensores");
	                    System.out.println("No se encontraron sensores");
	                } else {
	                    routingContext.response()
	                        .putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(200)
	                        .end(result.encode());
	                    System.out.println("Se han encontrado los sensores");
	                    System.out.println(result);
	                }
	            } else {
	                routingContext.response()
	                    .putHeader("content-type", "application/json; charset=utf-8")
	                    .setStatusCode(500)
	                    .end("Error al ejecutar la consulta: " + ar.cause().getMessage());
	            }
	        });
	}
	
	//AÑADIR UN SENSOR
	private void addOneSensor(RoutingContext routingContext) {
	    final Sensor aux = gson.fromJson(routingContext.getBodyAsString(), Sensor.class);
	    Integer idSensor = aux.getIdentificador();
	    Integer idPlaca = aux.getDeviceID();
	    String tipoSensor = aux.getTipo();
	    String nombre = aux.getNombre();

	    if (idSensor == null || idPlaca == null || tipoSensor == null || nombre == null) {
	        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	            .setStatusCode(400)
	            .end("Campos requeridos (NOT NULL): idSensor, idPlaca, tipoSensor, nombre");
	        return;
	    }

	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            SqlConnection conn = connection.result();

	            // Comprobar si ya existe
	            conn.preparedQuery("SELECT * FROM SENSOR WHERE idSensor = ?")
	                .execute(Tuple.of(idSensor), selectRes -> {
	                    if (selectRes.succeeded()) {
	                        if (selectRes.result().rowCount() > 0) {
	                            routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                                .setStatusCode(409) // Conflict
	                                .end("Ya existe un sensor con idSensor = " + idSensor);
	                            conn.close();
	                        } else {
	                            conn.preparedQuery("INSERT INTO SENSOR(idSensor, nombre, tipo, idDispositivo) VALUES (?, ?, ?, ?)")
	                                .execute(Tuple.of(idSensor, nombre, tipoSensor, idPlaca), insertRes -> {
	                                    if (insertRes.succeeded()) {
	                                        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                                            .setStatusCode(201)
	                                            .end("Sensor añadido");
	                                    } else {
	                                        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                                            .setStatusCode(500)
	                                            .end("Error al añadir: " + insertRes.cause().getMessage());
	                                    }
	                                    conn.close();
	                                });
	                        }
	                    } else {
	                        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                            .setStatusCode(500)
	                            .end("Error al comprobar existencia: " + selectRes.cause().getMessage());
	                        conn.close();
	                    }
	                });
	        } else {
	            routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                .setStatusCode(500)
	                .end("Error al conectar con la base de datos: " + connection.cause().getMessage());
	        }
	    });
	}
	
	//ELIMINAR UN SENSOR
	private void deleteOneSensor(RoutingContext routingContext) {
		 int id = 0;
		    try {
		        id = Integer.parseInt(routingContext.request().getParam("id"));
		        final int sensorId = id;

		        mySqlClient.getConnection(conn -> {
		            if (conn.succeeded()) {
		                SqlConnection connection = conn.result();
		                connection
		                    .preparedQuery("DELETE FROM Sensor WHERE idSensor = ?;")
		                    .execute(Tuple.of(sensorId), ar -> {
		                        if (ar.succeeded()) {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(200)
		                                .end("{\"message\":\"Sensor eliminado correctamente\"}");
		                            System.out.println("Sensor eliminado correctamente");
		                        } else {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(500)
		                                .end("Error al ejecutar la eliminación: " + ar.cause().toString());
		                            System.out.println("Error: " + ar.cause().toString());
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
	
	//MODIFICAR UN SENSOR
	private void putOneSensor(RoutingContext routingContext) {
		 JsonObject body = routingContext.getBodyAsJson();
		 String idParam = routingContext.pathParam("id");
		    if (body == null || idParam==null || !body.containsKey("name")
		        || !body.containsKey("type") || !body.containsKey("deviceID")) {
		        routingContext.response()
		            .putHeader("content-type", "application/json; charset=utf-8")
		            .setStatusCode(400)
		            .end("Error: Faltan parámetros en el cuerpo de la petición.");
		        return;
		    }

		    int id = Integer.parseInt(idParam);
		    String name = body.getString("name");
		    String type = body.getString("type");
		    int dispositivoId = body.getInteger("deviceID");

		    mySqlClient
		        .preparedQuery("UPDATE Sensor SET nombre = ?, tipo = ?, idDispositivo = ? WHERE idSensor = ?;")
		        .execute(Tuple.of(name, type, dispositivoId, id), ar -> {
		            if (ar.succeeded()) {
		                if (ar.result().rowCount() == 0) {
		                    routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(404)
		                        .end("No se encontró el sensor con id: " + id);
		                    System.out.println("No se encontró el sensor con id: " + id);
		                } else {
		                    routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(200)
		                        .end("{\"message\":\"Sensor actualizado correctamente\"}");
		                    System.out.println("Sensor actualizado correctamente");
		                }
		            } else {
		                routingContext.response()
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .setStatusCode(500)
		                    .end("Error al ejecutar la actualización: " + ar.cause().getMessage());
		            }
		        });
	}
	
	//OBTENER UN SENSOR POR SU ID
	private void getSensorbyId(RoutingContext routingContext){
		int id = 0;
		try {
			id = Integer.parseInt(routingContext.request().getParam("id"));
			final int comp = id;
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery("SELECT * FROM Sensor WHERE idSensor = ? LIMIT 1;").execute(
							Tuple.of(comp),
							res -> {
								if (res.succeeded()) {
									// Get the result set
									RowSet<Row> resultSet = res.result();
									JsonArray result = new JsonArray();
									for (Row row : resultSet) {
										result.add(JsonObject.mapFrom(new Sensor(
												row.getInteger("idSensor"),
												row.getString("nombre"),
												row.getString("tipo"),
												row.getInteger("idDispositivo"))));
									}
									if (result.isEmpty()) {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(404).end("No se encontraron sensores");
										System.out.println("No se ha encontrado el sensor");
									} else {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
										System.out.println("Sensor encontrado: ");
										System.out.println(result);
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
	
	//OBTENER VALORES DE UN SENSOR ESPECÍFICO
	private void getValuesbySensorId(RoutingContext routingContext) {
		int id = 0;
		try {
			id = Integer.parseInt(routingContext.request().getParam("id_sensor"));
			final int comp = id;
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery("SELECT * FROM SENSORVALUE WHERE idSensor = ?").execute(
							Tuple.of(comp),
							res -> {
								if (res.succeeded()) {
									// Get the result set
									RowSet<Row> resultSet = res.result();
									JsonArray result = new JsonArray();
									for (Row row : resultSet) {
										result.add(JsonObject.mapFrom(new Sensor(
												row.getInteger("idSensorValue"),
												row.getString("idSensor"),
												row.getString("value"),
												row.getInteger("timeStamp"))));
									}
									if (result.isEmpty()) {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(404).end("No se encontraron sensores");
										System.out.println("No se ha encontrado el sensor");
									} else {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
										System.out.println("Valores del sensor encontrado: ");
										System.out.println(result);
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

	//AÑADIR UN VALOR
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
					"INSERT INTO SensorValue(idSensorValue,idSensor, value, timeStamp) VALUES (?, ?, ?, ?);")
					.execute(Tuple.of(idSensorValue,sensorId, value, timestamp), res -> {
						if (res.succeeded()) {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(201).end("Valor del sensor añadido");
							System.out.println("Valor del sensor añadido: ");
							System.out.println(res);
						} else {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(500).end("Error al añadir: " + res.cause().getLocalizedMessage());
							System.out.println("Error al añadir: " + res.cause().getLocalizedMessage());
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
	
	//OBTENER TODOS LOS ACTUADORES
	private void getAllActuators(RoutingContext routingContext) {
		mySqlClient
        .preparedQuery("SELECT * FROM Actuador;")
        .execute(ar -> {
            if (ar.succeeded()) {
                RowSet<Row> resultSet = ar.result();
                JsonArray result = new JsonArray();
                for (Row row : resultSet) {
                    result.add(JsonObject.mapFrom(new Actuador(
                    		row.getInteger("idActuador"),
							row.getString("nombre"),
							row.getString("tipo"),
							row.getInteger("idDispositivoAct"))));
                }

                if (result.isEmpty()) {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(404)
                        .end("No se encontraron actuadores");
                    System.out.println("No se encontraron actuadores");
                } else {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(result.encode());
                    System.out.println("Actuadores: ");
                    System.out.println(result);
                }
            } else {
                routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end("Error al ejecutar la consulta: " + ar.cause().getMessage());
            }
        });
	}


	//AÑADIR UN ACTUADOR
	private void addOneActuator(RoutingContext routingContext) {
	    final Actuador aux = gson.fromJson(routingContext.getBodyAsString(), Actuador.class);
	    Integer idActuador = aux.getId();
	    Integer idPlaca = aux.getDispositivoId();
	    String tipoActuador = aux.getType();
	    String nombre = aux.getName();

	    if (idActuador == null || idPlaca == null || tipoActuador == null || nombre == null) {
	        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	            .setStatusCode(400)
	            .end("Campos requeridos (NOT NULL):\n\tidActuador\n\tidPlaca\n\ttipoActuador\n\tnombre");
	        return;
	    }

	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            SqlConnection conn = connection.result();

	            // Comprobar si ya existe el actuador
	            conn.preparedQuery("SELECT * FROM Actuador WHERE idActuador = ?")
	                .execute(Tuple.of(idActuador), selectRes -> {
	                    if (selectRes.succeeded()) {
	                        if (selectRes.result().rowCount() > 0) {
	                            // Ya existe, no insertar
	                            routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                                .setStatusCode(409) // Conflict
	                                .end("Ya existe un actuador con id = " + idActuador);
	                            conn.close();
	                        } else {
	                            // No existe, insertar
	                            conn.preparedQuery("INSERT INTO Actuador(idActuador, nombre, tipo, idDispositivoAct) VALUES (?, ?, ?, ?);")
	                                .execute(Tuple.of(idActuador, nombre, tipoActuador, idPlaca), res -> {
	                                    if (res.succeeded()) {
	                                        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                                            .setStatusCode(201)
	                                            .end("Actuador añadido");
	                                        System.out.println("Actuador añadido correctamente:");
	                                        System.out.println(aux);
	                                    } else {
	                                        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                                            .setStatusCode(500)
	                                            .end("Error al añadir: " + res.cause().getLocalizedMessage());
	                                        System.out.println("Error al añadir: " + res.cause().getLocalizedMessage());
	                                    }
	                                    conn.close();
	                                });
	                        }
	                    } else {
	                        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                            .setStatusCode(500)
	                            .end("Error al comprobar existencia: " + selectRes.cause().getLocalizedMessage());
	                        conn.close();
	                    }
	                });
	        } else {
	            routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                .setStatusCode(500)
	                .end("Error al conectar: " + connection.cause().toString());
	        }
	    });
	}

	
	//BORRAR
	private void deleteOneActuator(RoutingContext routingContext) {
		 int id = 0;
		    try {
		        id = Integer.parseInt(routingContext.request().getParam("id"));
		        final int sensorId = id;

		        mySqlClient.getConnection(conn -> {
		            if (conn.succeeded()) {
		                SqlConnection connection = conn.result();
		                connection
		                    .preparedQuery("DELETE FROM Actuador WHERE idActuador = ?;")
		                    .execute(Tuple.of(sensorId), ar -> {
		                        if (ar.succeeded()) {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(200)
		                                .end("{\"message\":\"Actuador eliminado correctamente\"}");
		                            System.out.println("Actuador eliminado correctamente");
		                        } else {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(500)
		                                .end("Error al ejecutar la eliminación: " + ar.cause().toString());
		                            System.out.println("Error: "+ ar.cause().toString());
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
		 String idParam = routingContext.pathParam("id");
		 
		    if (body == null || idParam==null || !body.containsKey("name")
		        || !body.containsKey("type") || !body.containsKey("dispositivoId")) {
		        routingContext.response()
		            .putHeader("content-type", "application/json; charset=utf-8")
		            .setStatusCode(400)
		            .end("Error: Faltan parámetros en el cuerpo de la petición.");
		        return;
		    }

		    int id = Integer.parseInt(idParam);
		    String name = body.getString("name");
		    String type = body.getString("type");
		    int dispositivoId = body.getInteger("dispositivoId");

		    mySqlClient
		        .preparedQuery("UPDATE Actuador SET nombre = ?, tipo = ?, idDispositivoAct = ? WHERE idActuador = ?;")
		        .execute(Tuple.of(name, type, dispositivoId, id), ar -> {
		            if (ar.succeeded()) {
		                if (ar.result().rowCount() == 0) {
		                    routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(404)
		                        .end("No se encontró el actuador con id: " + id);
		                    System.out.println("No se encontró el actuador con id: " + id);
		                } else {
		                    routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(200)
		                        .end("{\"message\":\"Actuador actualizado correctamente\"}");
		                    System.out.println("Actuador actualizado correctamente");
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
												row.getInteger("idActuador"),
												row.getString("nombre"),
												row.getString("tipo"),
												row.getInteger("idDispositivoAct"))));
									}
									if (result.isEmpty()) {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(404).end("No se encontraron actuadores");
										System.out.println("No se ha encontrado ela actuador");
									} else {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
										System.out.println("Actuador encontrado:");
										System.out.println(result);
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
	
	private void getStatesbyActuatorId(RoutingContext routingContext) {
		int id = 0;
		try {
			id = Integer.parseInt(routingContext.request().getParam("id_actuador"));
			final int comp = id;
			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery("SELECT * FROM ACTUADORSTATE WHERE idActuador = ?").execute(
							Tuple.of(comp),
							res -> {
								if (res.succeeded()) {
									// Get the result set
									RowSet<Row> resultSet = res.result();
									JsonArray result = new JsonArray();
									for (Row row : resultSet) {
										result.add(JsonObject.mapFrom(new ActuadorState(
												row.getInteger("idActuadorState"),
												row.getInteger("idActuador"),
												row.getBoolean("estado"),
												row.getString("valor"),
												row.getLong("timeStamp")
												)));
									}
									if (result.isEmpty()) {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(404).end("No se encontraron estados");
										System.out.println("No se ha encontrado el actuador");
									} else {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
										System.out.println("Estados del actuador encontrado: ");
										System.out.println(result);
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

	private void addOneState(RoutingContext routingContext) {
		final ActuadorState aux = gson.fromJson(routingContext.getBodyAsString(), ActuadorState.class);
		Integer idEstado = aux.getId();
		Boolean estado = aux.getEstado();
		String valor = aux.getValor();
		Long timestamp = aux.getTimestamp();
		Integer actuadorId = aux.getId_actuador();

		if (idEstado == null || estado == null || timestamp == null || actuadorId == null || valor == null) {
			routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
			.setStatusCode(400).end("Campos requeridos (NOT NULL):\n\tidEstado\n\testado\n\ttimestamp\n\tactuadorId");
			return;
		}

		mySqlClient.getConnection(connection -> {
			if (connection.succeeded()) {
				connection.result().preparedQuery(
					"INSERT INTO ActuadorState(idActuadorState, estado, valor,timeStamp, idActuador) VALUES (?, ?, ?, ?, ?);")
					.execute(Tuple.of(idEstado, estado, valor, timestamp, actuadorId), res -> {
						if (res.succeeded()) {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(201).end("Estado del actuador añadido");
							System.out.println("Estado del actuador añadido: ");
							System.out.println(res);
						} else {
							routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
							.setStatusCode(500).end("Error al añadir: " + res.cause().getLocalizedMessage());
							System.out.println("Error al añadir: " + res.cause().getLocalizedMessage());
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
                    		row.getInteger("idDispositivo"),
							row.getString("nombre"),
							row.getInteger("idGrupo")
							)));
                }

                if (result.isEmpty()) {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(404)
                        .end("No se encontraron dispositivos");
                    System.out.println("No se encontraron dispositivos");
                } else {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(result.encode());
                    System.out.println("");
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
							"SELECT * FROM Dispositivo WHERE idDispositivo = ? LIMIT 1;").execute(
							Tuple.of(comp),
							res -> {
								if (res.succeeded()) {
									// Get the result set
									RowSet<Row> resultSet = res.result();
									JsonArray result = new JsonArray();
									for (Row elem : resultSet) {
										result.add(JsonObject.mapFrom(new Dispositivo(
												elem.getInteger("idDispositivo"),
												elem.getString("nombre"),
												elem.getInteger("idGrupo"))));
									}
									if (result.isEmpty()) {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(404).end("No se encontraron placas");
										System.out.println("No se ha encontrado el dispositivo");
									} else {
										routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
										System.out.println("Dispositivo encontrado: ");
										System.out.println(result);
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


	private void addOneDevice(RoutingContext routingContext) {
	    final Dispositivo aux = gson.fromJson(routingContext.getBodyAsString(), Dispositivo.class);
	    Integer idDispositivo = aux.getId();
	    String nombre = aux.getNombre();
	    Integer grupoId = aux.getIdGrupo();

	    if (idDispositivo == null || nombre == null || grupoId == null) {
	        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	            .setStatusCode(400)
	            .end("Campos requeridos (NOT NULL):\n\tidDispositivo\n\tnombre\n\tgrupoId");
	        return;
	    }

	    mySqlClient.getConnection(connection -> {
	        if (connection.succeeded()) {
	            SqlConnection conn = connection.result();

	            conn.preparedQuery("SELECT * FROM Dispositivo WHERE idDispositivo = ?")
	                .execute(Tuple.of(idDispositivo), selectRes -> {
	                    if (selectRes.succeeded()) {
	                        if (selectRes.result().rowCount() > 0) {
	                            routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                                .setStatusCode(409)
	                                .end("Ya existe un dispositivo con id = " + idDispositivo);
	                            conn.close();
	                        } else {
	                            conn.preparedQuery("INSERT INTO Dispositivo(idDispositivo, nombre, idGrupo) VALUES (?, ?, ?);")
	                                .execute(Tuple.of(idDispositivo, nombre, grupoId), res -> {
	                                    if (res.succeeded()) {
	                                        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                                            .setStatusCode(201)
	                                            .end("Dispositivo añadido");
	                                        System.out.println("Dispositivo añadido:");
	                                        System.out.println(aux);
	                                    } else {
	                                        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                                            .setStatusCode(500)
	                                            .end("Error al añadir: " + res.cause().getLocalizedMessage());
	                                        System.out.println("Error al añadir: " + res.cause().getLocalizedMessage());
	                                    }
	                                    conn.close();
	                                });
	                        }
	                    } else {
	                        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                            .setStatusCode(500)
	                            .end("Error al comprobar existencia: " + selectRes.cause().getLocalizedMessage());
	                        conn.close();
	                    }
	                });
	        } else {
	            routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                .setStatusCode(500)
	                .end("Error al conectar: " + connection.cause().toString());
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
		                    .preparedQuery("DELETE FROM Dispositivo WHERE idDispositivo = ?;")
		                    .execute(Tuple.of(sensorId), ar -> {
		                        if (ar.succeeded()) {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(200)
		                                .end("{\"message\":\"Dispositivo eliminado correctamente\"}");
		                            System.out.println("Dispositivo eliminado correctamente");
		                        } else {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(500)
		                                .end("Error al ejecutar la eliminación: " + ar.cause().toString());
		                            System.out.println("Error: " + ar.cause().toString());
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
		String idParam = routingContext.pathParam("id");

	    if (body == null || idParam == null || !body.containsKey("name") || !body.containsKey("grupoId")) {
	        routingContext.response()
	            .putHeader("content-type", "application/json; charset=utf-8")
	            .setStatusCode(400)
	            .end("Error: Faltan parámetros en el cuerpo de la petición.");
	        return;
	    }

	    int id = Integer.parseInt(idParam);
	    String name = body.getString("name");
	    int grupoId = body.getInteger("grupoId");

	    mySqlClient
	        .preparedQuery("UPDATE Dispositivo SET nombre = ?, idGrupo = ? WHERE idDispositivo = ?;")
	        .execute(Tuple.of(name, grupoId, id), ar -> {
	            if (ar.succeeded()) {
	                if (ar.result().rowCount() == 0) {
	                    routingContext.response()
	                        .putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(404)
	                        .end("No se encontró el dispositivo con id: " + id);
	                    System.out.println("No se encontró el dispositivo con id: " + id);
	                } else {
	                    routingContext.response()
	                        .putHeader("content-type", "application/json; charset=utf-8")
	                        .setStatusCode(200)
	                        .end("{\"message\":\"Dispositivo actualizado correctamente\"}");
	                    System.out.println("Dispositivo actualizado correctamente");
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
                    		row.getInteger("idGrupo"),
							row.getString("canalMQTT"),
							row.getString("nombre")
							)));
                }

                if (result.isEmpty()) {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(404)
                        .end("No se encontraron grupos");
                    System.out.println("No se encontraton grupos");
                } else {
                    routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .setStatusCode(200)
                        .end(result.encode());
                    System.out.println("Grupos: ");
                    System.out.println(result);
                }
            } else {
                routingContext.response()
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .setStatusCode(500)
                    .end("Error al ejecutar la consulta: " + ar.cause().getMessage());
            }
        });
	}

	private void getGroupById(RoutingContext routingContext) {
		int id = 0;
		try {
			id = Integer.parseInt(routingContext.request().getParam("id"));
			final int grupoId = id;

			mySqlClient.getConnection(connection -> {
				if (connection.succeeded()) {
					connection.result().preparedQuery(
							"SELECT * FROM Grupo WHERE idGrupo = ? LIMIT 1;")
						.execute(Tuple.of(grupoId), res -> {
							if (res.succeeded()) {
								RowSet<Row> resultSet = res.result();
								JsonArray result = new JsonArray();
								for (Row elem : resultSet) {
									result.add(JsonObject.mapFrom(new Grupo(
										elem.getInteger("idGrupo"),
										elem.getString("canalMQTT"),
										elem.getString("nombre")
									)));
								}
								if (result.isEmpty()) {
									routingContext.response()
										.putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(404).end("No se encontró ningún grupo con ese id");
									System.out.println("No se encontró ningún grupo con ese id");
								} else {
									routingContext.response()
										.putHeader("content-type", "application/json; charset=utf-8")
										.setStatusCode(200).end(result.toString());
									System.out.println("Grupo encontrado: ");
									System.out.println(result);
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
	            SqlConnection conn = connection.result();


	            conn.preparedQuery("SELECT * FROM Grupo WHERE idGrupo = ?")
	                .execute(Tuple.of(idGrupo), selectRes -> {
	                    if (selectRes.succeeded()) {
	                        if (selectRes.result().rowCount() > 0) {
	                            routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                                .setStatusCode(409).end("Ya existe un grupo con id = " + idGrupo);
	                            conn.close();
	                        } else {
	                            conn.preparedQuery("INSERT INTO Grupo(idGrupo, canalMQTT, nombre) VALUES (?, ?, ?);")
	                                .execute(Tuple.of(idGrupo, canalMQTT, nombre), res -> {
	                                    if (res.succeeded()) {
	                                        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                                            .setStatusCode(201).end("Grupo añadido");
	                                        System.out.println("Grupo añadido correctamente: ");
	                                        System.out.println(aux);
	                                    } else {
	                                        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                                            .setStatusCode(500).end("Error al añadir: " + res.cause().getLocalizedMessage());
	                                        System.out.println("Error al añadir: " + res.cause().getLocalizedMessage());
	                                        System.out.println(aux);
	                                    }
	                                    conn.close();
	                                });
	                        }
	                    } else {
	                        routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
	                            .setStatusCode(500).end("Error al comprobar existencia: " + selectRes.cause().getLocalizedMessage());
	                        conn.close();
	                    }
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
		        final int grupoId = id;

		        mySqlClient.getConnection(conn -> {
		            if (conn.succeeded()) {
		                SqlConnection connection = conn.result();
		                connection
		                    .preparedQuery("DELETE FROM Grupo WHERE idGrupo = ?;")
		                    .execute(Tuple.of(grupoId), ar -> {
		                        if (ar.succeeded()) {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(200)
		                                .end("{\"message\":\"Grupo eliminado correctamente\"}");
		                            System.out.println("Grupo con id "+grupoId+" eliminado correctamente.");
		                        } else {
		                            routingContext.response()
		                                .putHeader("content-type", "application/json; charset=utf-8")
		                                .setStatusCode(500)
		                                .end("Error al ejecutar la eliminación: " + ar.cause().toString());
		                            System.out.println("Error: " + ar.cause().toString());
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
		 String idParam = routingContext.pathParam("id");

		    if (body == null || idParam ==null || !body.containsKey("name") || !body.containsKey("canal_mqtt")) {
		        routingContext.response()
		            .putHeader("content-type", "application/json; charset=utf-8")
		            .setStatusCode(400)
		            .end("Error: Faltan parámetros en el cuerpo de la petición.");
		        return;
		    }

		    int id = Integer.parseInt(idParam);
		    String name = body.getString("name");
		    String canalMQTT = body.getString("canal_mqtt");

		    mySqlClient
		        .preparedQuery("UPDATE Grupo SET nombre = ?, canalMQTT = ? WHERE idGrupo = ?;")
		        .execute(Tuple.of(name, canalMQTT, id), ar -> {
		            if (ar.succeeded()) {
		                if (ar.result().rowCount() == 0) {
		                    routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(404)
		                        .end("No se encontró el grupo con id: " + id);
		                    System.out.println("No se encontró el grupo con id: " + id);
		                } else {
		                    routingContext.response()
		                        .putHeader("content-type", "application/json; charset=utf-8")
		                        .setStatusCode(200)
		                        .end("{\"message\":\"Grupo actualizado correctamente\"}");
		                    System.out.println("Grupo con id " + id + " actualizado correctamente");
		                }
		            } else {
		                routingContext.response()
		                    .putHeader("content-type", "application/json; charset=utf-8")
		                    .setStatusCode(500)
		                    .end("Error al ejecutar la actualización: " + ar.cause().getMessage());
		                System.out.println("Error: "+ ar.cause().getMessage());
		            }
		        });
	}
	
	
	//FUNCIONES DE CREACION DE DATOS
	
/*	private void createSomeSensors(int number) {
		Random rnd = new Random();
		IntStream.range(0, number).forEach(elem -> {
			int id = rnd.nextInt();
			sensors.put(id, new Sensor(0+id,"Name" + id, "Type" + id, 1+id));
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
	}*/
		

	
}
