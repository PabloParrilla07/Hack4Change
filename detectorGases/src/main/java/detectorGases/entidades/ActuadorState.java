package detectorGases.entidades;

public class ActuadorState {
	//id (PK), id_actuador (FK), estado (boolean), timestamp
	//(datetime)

	protected Integer actuadorStateId; //(PK)
	protected Integer actuadorId; //(FK)
	protected Boolean state; //¿Encendido o apagado?
	protected Long timestamp;
	
	public ActuadorState(Integer id, Integer id_actuador, Boolean estado, Long timestamp) {
		super();
		this.actuadorStateId = id;
		this.actuadorId = id_actuador;
		this.state = estado;
		this.timestamp = timestamp;
	}

	public Integer getId() {
		return actuadorStateId;
	}

	public void setId(Integer id) {
		this.actuadorStateId = id;
	}

	public Integer getId_actuador() {
		return actuadorId;
	}

	public void setId_actuador(Integer id_actuador) {
		this.actuadorId = id_actuador;
	}

	public Boolean getEstado() {
		return state;
	}

	public void setEstado(Boolean estado) {
		this.state = estado;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	} 
	
	
	
}
