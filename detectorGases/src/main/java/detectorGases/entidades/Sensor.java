package detectorGases.entidades;

import java.util.Objects;

public class Sensor{
	
	//FK: Foreign Key
	
	//2 Opciones:
		//-Una sola clase sensors
		//-Una sola clase sensors, y una para cada sensor usando herencia.

	protected String name;
	protected String type;
	protected int sensorId;//(PK)
	protected int deviceID; //(FK) Id de la esp32 a las que estas conectado

		
	public Sensor(String nombre, String tipo, int identificador, int deviceID) {
		super();
		this.name = nombre;
		this.type = tipo;
		this.sensorId = identificador;
		this.deviceID = deviceID;
	}


	public String getNombre() {
		return name;
	}


	public void setNombre(String nombre) {
		this.name = nombre;
	}


	public String getTipo() {
		return type;
	}


	public void setTipo(String tipo) {
		this.type = tipo;
	}


	public int getIdentificador() {
		return sensorId;
	}


	public void setIdentificador(int identificador) {
		this.sensorId = identificador;
	}


	public int getDeviceID() {
		return deviceID;
	}


	public void setDeviceID(int deviceID) {
		this.deviceID = deviceID;
	}


	@Override
	public int hashCode() {
		return Objects.hash(deviceID, sensorId, name, type);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sensor other = (Sensor) obj;
		return deviceID == other.deviceID && sensorId == other.sensorId
				&& Objects.equals(name, other.name) && Objects.equals(type, other.type);
	}


	@Override
	public String toString() {
		return "Sensor [nombre=" + name + ", tipo=" + type + ", identificador=" + sensorId + ", deviceID="
				+ deviceID + "]";
	}
	
	
		
		
}


