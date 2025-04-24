package detectorGases.entidades;

public class Actuador {
	
	protected Integer id; //(PK)
	protected String name;
	protected String type;
	protected Integer deviceID;//(FK)
	
	public Actuador(Integer id, String name, String type, Integer deviceID) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.deviceID = deviceID;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
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
	
	public Integer getDeviceID() {
		return deviceID;
	}
	
	public void setDeviceID(Integer deviceID) {
		this.deviceID = deviceID;
	}

}
