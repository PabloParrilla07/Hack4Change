package detectorGases;

import detectorGases.entidades.Actuador;
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
//	getAllAtuadoresState();
//	getAllDispositivos();
//	getAllGrupos();
//	getAllSensores();
//	getAllSensoresPMS();
//	getAllSensoresValues();
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
}
