package detectorGases.entidades;

public class Actuador {
	
	protected Integer actuadorId; //(PK)
	protected String name;
	protected String type;
	protected Integer dispositivoId;//(FK)
	
	public Actuador(Integer id, String name, String type, Integer dispositivoId) {
		super();
		this.actuadorId = id;
		this.name = name;
		this.type = type;
		this.dispositivoId = dispositivoId;
	}
	
	public Integer getId() {
		return actuadorId;
	}
	
	public void setId(Integer id) {
		this.actuadorId = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Integer getDispositivoId() {
		return dispositivoId;
	}
	
	public void setDispositivoId(Integer dispositivoId) {
		this.dispositivoId = dispositivoId;
	}

}
