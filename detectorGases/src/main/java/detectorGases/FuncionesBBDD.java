package detectorGases;

import detectorGases.entidades.Actuador;
import detectorGases.entidades.ActuadorState;
import detectorGases.entidades.Dispositivo;
import detectorGases.entidades.Grupo;
import detectorGases.entidades.Sensor;
import detectorGases.entidades.SensorPMS;
import detectorGases.entidades.SensorValue;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class FuncionesBBDD extends AbstractVerticle{
	
	MySQLPool mySqlClient;

	@Override
	public void start(Promise<Void> startFuture) {
	MySQLConnectOptions connectOptions = new MySQLConnectOptions().setPort(1883).setHost("127.0.0.1")
			.setDatabase("IoTAmaso").setUser("IoTAmaso").setPassword("I0T4m4s0");
	
	PoolOptions poolOptions = new PoolOptions().setMaxSize(10);
	
	mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);
	
	getAllActuadores();
	getAllActuadoresState();
	getAllDispositivos();
	getAllGrupos();
	getAllSensores();
	getAllSensoresPMS();
	getAllSensoresValues();
//	
//	getOneActuador();
//	getOneActuadorState();
//	getOneDispositivo();
//	getOneGrupo();
//	getOneSensor();
//	getOneSensorPMS();
//	getOneSensorValue();
	
//	addOneActuador();
	
	}
	
	@Override
	public void stop(Promise<Void> stopPromise) throws Exception {
		try {
			stopPromise.complete();
		} catch (Exception e) {
			stopPromise.fail(e);
		}
		super.stop(stopPromise);
	}
	
	private void getAllActuadores() {
		mySqlClient.query("SELECT * FROM Actuador").execute(res -> {
			if(res.succeeded()) {
				RowSet<Row> resultSet = res.result();
				System.out.println("Actuadores");
				System.out.println(resultSet.size());
				JsonArray result = new JsonArray();
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Actuador(
							elem.getInteger("actuadorId"),
							elem.getString("name"),
							elem.getString("type"),
							elem.getInteger("dispositivoId"))));
			}
			
				System.out.println(result.toString());
		}else {
			System.out.println("Error: " + res.cause().getLocalizedMessage());
		}
		System.out.println();
		});
	}
	
	private void getAllActuadoresState() {
		mySqlClient.query("SELECT * FROM ActuadorState").execute(res -> {
			if(res.succeeded()) {
				RowSet<Row> resultSet = res.result();
				System.out.println("Actuadores State");
				System.out.println(resultSet.size());
				JsonArray result = new JsonArray();
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new ActuadorState(
							elem.getInteger("actuadorStateId"),
							elem.getInteger("actuadorId"),
							elem.getBoolean("state"),
							elem.getLong("timestamp")
							)));
			}
			
				System.out.println(result.toString());
		}else {
			System.out.println("Error: " + res.cause().getLocalizedMessage());
		}
		System.out.println();
		});
	}
	
	private void getAllDispositivos() {
		mySqlClient.query("SELECT * FROM Dispositivo").execute(res -> {
			if(res.succeeded()) {
				RowSet<Row> resultSet = res.result();
				System.out.println("Dispostivos");
				System.out.println(resultSet.size());
				JsonArray result = new JsonArray();
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Dispositivo(
							elem.getInteger("dispositivoId"),
							elem.getString("name"),
							elem.getInteger("grupoId")
							)));
			}
			
				System.out.println(result.toString());
		}else {
			System.out.println("Error: " + res.cause().getLocalizedMessage());
		}
		System.out.println();
		});
	}
	
	private void getAllGrupos() {
		mySqlClient.query("SELECT * FROM Grupo").execute(res -> {
			if(res.succeeded()) {
				RowSet<Row> resultSet = res.result();
				System.out.println("Grupos");
				System.out.println(resultSet.size());
				JsonArray result = new JsonArray();
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Grupo(
							elem.getInteger("grupoId"),
							elem.getString("canalMQTT"),
							elem.getString("name")
							)));
			}
			
				System.out.println(result.toString());
		}else {
			System.out.println("Error: " + res.cause().getLocalizedMessage());
		}
		System.out.println();
		});
	}
	
	private void getAllSensores() {
		mySqlClient.query("SELECT * FROM Sensor").execute(res -> {
			if(res.succeeded()) {
				RowSet<Row> resultSet = res.result();
				System.out.println("Sensores");
				System.out.println(resultSet.size());
				JsonArray result = new JsonArray();
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new Sensor(
							elem.getString("type"),
							elem.getString("name"),
							elem.getInteger("sensorId"),
							elem.getInteger("dispositivoId")
							)));
			}
			
				System.out.println(result.toString());
		}else {
			System.out.println("Error: " + res.cause().getLocalizedMessage());
		}
		System.out.println();
		});
	}

	private void getAllSensoresPMS() {
		mySqlClient.query("SELECT * FROM SensorPMS").execute(res -> {
			if(res.succeeded()) {
				RowSet<Row> resultSet = res.result();
				System.out.println("Sensores PMS");
				System.out.println(resultSet.size());
				JsonArray result = new JsonArray();
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new SensorPMS(
							elem.getInteger("sensorPMSId"),
							elem.getInteger("sensorId"),
							elem.getFloat("valuePM10"),
							elem.getFloat("valuePM1"),
							elem.getFloat("valuePM25"),
							elem.getLong("timestamp")
							)));
			}
			
				System.out.println(result.toString());
		}else {
			System.out.println("Error: " + res.cause().getLocalizedMessage());
		}
		System.out.println();
		});
	}
	
	private void getAllSensoresValues() {
		mySqlClient.query("SELECT * FROM SensorValue").execute(res -> {
			if(res.succeeded()) {
				RowSet<Row> resultSet = res.result();
				System.out.println("Sensores Values");
				System.out.println(resultSet.size());
				JsonArray result = new JsonArray();
				for (Row elem : resultSet) {
					result.add(JsonObject.mapFrom(new SensorValue(
							elem.getInteger("sensorValueId"),
							elem.getInteger("sensorId"),
							elem.getFloat("value"),
							elem.getLong("timestamp")
							)));
			}
			
				System.out.println(result.toString());
		}else {
			System.out.println("Error: " + res.cause().getLocalizedMessage());
		}
		System.out.println();
		});
	}
	
	private void addOneActuador(String name, String type, int dispositivoId) {
		  String insertSql = "INSERT INTO Actuador (name, type, dispositivoId) VALUES (?, ?, ?)";
		    
		    mySqlClient
		        .preparedQuery(insertSql)
		        .execute(Tuple.of(name, type, dispositivoId), res -> {
		            if (res.succeeded()) {
		                System.out.println("Actuador insertado correctamente.");
		            } else {
		                System.out.println("Error al insertar actuador: " + res.cause().getMessage());
		            }
		        });
	}
	
	private void addOneActuadorState(boolean state, Long timestamp, int actuadorId) {
		  String insertSql = "INSERT INTO ActuadorState (state, timestamp, actuadorId) VALUES (?, ?, ?)";
		    
		    mySqlClient
		        .preparedQuery(insertSql)
		        .execute(Tuple.of(state, timestamp, actuadorId), res -> {
		            if (res.succeeded()) {
		                System.out.println("Actuador insertado correctamente.");
		            } else {
		                System.out.println("Error al insertar actuador: " + res.cause().getMessage());
		            }
		        });
		
	}
	
	private void addOneDispositivo(String name, int grupoId) {
		  String insertSql = "INSERT INTO Dispositivo (name, grupoId) VALUES (?, ?)";
		    
		    mySqlClient
		        .preparedQuery(insertSql)
		        .execute(Tuple.of(name, grupoId), res -> {
		            if (res.succeeded()) {
		                System.out.println("Actuador insertado correctamente.");
		            } else {
		                System.out.println("Error al insertar actuador: " + res.cause().getMessage());
		            }
		        });
	}
	
	private void addOneGrupo(String canalMQTT, String name) {
		  String insertSql = "INSERT INTO Grupo (canalMQTT, name) VALUES (?, ?)";
		    
		    mySqlClient
		        .preparedQuery(insertSql)
		        .execute(Tuple.of(canalMQTT, name), res -> {
		            if (res.succeeded()) {
		                System.out.println("Actuador insertado correctamente.");
		            } else {
		                System.out.println("Error al insertar actuador: " + res.cause().getMessage());
		            }
		        });
	}
	
	private void addOneSensor(String name, String type, int dispositivoId) {
		  String insertSql = "INSERT INTO Sensor (name, type, dispositivoId) VALUES (?, ?, ?)";
		    
		    mySqlClient
		        .preparedQuery(insertSql)
		        .execute(Tuple.of(name, type, dispositivoId), res -> {
		            if (res.succeeded()) {
		                System.out.println("Actuador insertado correctamente.");
		            } else {
		                System.out.println("Error al insertar actuador: " + res.cause().getMessage());
		            }
		        });
	}
	
	private void addOneSensorPMS(float valuePM10, float valuemPM1, float valuePM25, Long timestamp, int sensorId) {
		  String insertSql = "INSERT INTO SensorPMS (valuePM10, valuePM1, valuePM25, timestamp, sensorId) VALUES (?, ?, ?, ?, ?)";
		    
		    mySqlClient
		        .preparedQuery(insertSql)
		        .execute(Tuple.of(valuePM10, valuemPM1, valuePM25, timestamp, sensorId), res -> {
		            if (res.succeeded()) {
		                System.out.println("Actuador insertado correctamente.");
		            } else {
		                System.out.println("Error al insertar actuador: " + res.cause().getMessage());
		            }
		        });
	}
	
	private void addOneSensorValue(boolean state, Long timestamp, int actuadorId) {
		  String insertSql = "INSERT INTO SensorValue (state, timestamp, actuadorId) VALUES (?, ?, ?)";
		    
		    mySqlClient
		        .preparedQuery(insertSql)
		        .execute(Tuple.of(state, timestamp, actuadorId), res -> {
		            if (res.succeeded()) {
		                System.out.println("Actuador insertado correctamente.");
		            } else {
		                System.out.println("Error al insertar actuador: " + res.cause().getMessage());
		            }
		        });
		
	}
}
