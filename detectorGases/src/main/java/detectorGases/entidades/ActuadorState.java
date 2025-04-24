package detectorGases.entidades;

public class ActuadorState {
	//id (PK), id_actuador (FK), estado (boolean), timestamp
	//(datetime)

	protected Integer id; //(PK)
	protected Integer id_actuador; //(FK)
	protected Boolean estado; //¿Encendido o apagado?
	protected Long timestamp;
	
	public ActuadorState(Integer id, Integer id_actuador, Boolean estado, Long timestamp) {
		super();
		this.id = id;
		this.id_actuador = id_actuador;
		this.estado = estado;
		this.timestamp = timestamp;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId_actuador() {
		return id_actuador;
	}

	public void setId_actuador(Integer id_actuador) {
		this.id_actuador = id_actuador;
	}

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	} 
	
	
	
}
