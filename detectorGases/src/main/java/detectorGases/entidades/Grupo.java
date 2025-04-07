package detectorGases.entidades;

public class Grupo {

	protected int id;
	protected String canal_mqtt;
	protected String nombre;
	
	public Grupo(int id, String canal_mqtt, String nombre) {
		super();
		this.id = id;
		this.canal_mqtt = canal_mqtt;
		this.nombre = nombre;
	}
	
}
