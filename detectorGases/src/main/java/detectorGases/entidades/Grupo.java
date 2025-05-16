package detectorGases.entidades;

public class Grupo {

	protected int grupoId;
	protected String canal_mqtt;
	protected String name;
	
	public Grupo(int id, String canal_mqtt, String nombre) {
		super();
		this.grupoId = id;
		this.canal_mqtt = canal_mqtt;
		this.name = nombre;
	}

	public int getId() {
		return grupoId;
	}

	public void setId(int id) {
		this.grupoId = id;
	}

	public String getCanal_mqtt() {
		return canal_mqtt;
	}

	public void setCanal_mqtt(String canal_mqtt) {
		this.canal_mqtt = canal_mqtt;
	}

	public String getNombre() {
		return name;
	}

	public void setNombre(String nombre) {
		this.name = nombre;
	}
	
	
	
}
