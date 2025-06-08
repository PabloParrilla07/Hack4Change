package detectorGases;

import detectorGases.Rest.RestHighServer;
import detectorGases.Rest.RestLowServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

public class MainVerticle extends AbstractVerticle{
	
	public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
        //vertx.deployVerticle(new FuncionesBBDD());
    }
	
	public void start() {
		
		//DESPLEGAMOS EL SERVIDOR DE BAJO NIVEL
		vertx.deployVerticle(RestLowServer.class.getName(), res -> {
            if (res.succeeded()) {
                System.out.println("Servidor bajo nivel desplegado");
            } else {
                System.out.println("Error al desplegar el servidor de bajo nivel:");
                res.cause().printStackTrace();
            }
        });

        //DESPLEFAMOS EL SERVIDOR DE ALTO NIVEL
        vertx.deployVerticle(RestHighServer.class.getName(), res -> {
            if (res.succeeded()) {
                System.out.println("Servidor alto nivel desplegado");
            } else {
                System.out.println("Error al desplegar el servidor de alto nivel:");
                res.cause().printStackTrace();
            }
        });
    }
	
	public void stop(Promise<Void> stopFuture) throws Exception {
		getVertx().undeploy(RestHighServer.class.getName());
		System.out.println("Servidor alto nivel cerrado");
		getVertx().undeploy(RestLowServer.class.getName());
		System.out.println("Servidor bajo nivel cerrado");
		getVertx().undeploy(MainVerticle.class.getName());
		System.out.println("Verticle principal cerrado");
	}

}
