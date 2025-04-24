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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCanal_mqtt() {
		return canal_mqtt;
	}

	public void setCanal_mqtt(String canal_mqtt) {
		this.canal_mqtt = canal_mqtt;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	
	
}
