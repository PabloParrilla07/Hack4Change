package detectorGases.entidades;

import java.util.Objects;

public class Sensor{
	
	//FK: Foreign Key
	
	//2 Opciones:
		//-Una sola clase sensors
		//-Una sola clase sensors, y una para cada sensor usando herencia.

	protected String nombre;
	protected String tipo;
	protected int id;//(PK)
	protected int deviceID; //(FK) Id de la esp32 a las que estas conectado

		
	public Sensor(String nombre, String tipo, int identificador, int deviceID) {
		super();
		this.nombre = nombre;
		this.tipo = tipo;
		this.id = identificador;
		this.deviceID = deviceID;
	}


	public String getNombre() {
		return nombre;
	}


	public void setNombre(String nombre) {
		this.nombre = nombre;
	}


	public String getTipo() {
		return tipo;
	}


	public void setTipo(String tipo) {
		this.tipo = tipo;
	}


	public int getIdentificador() {
		return id;
	}


	public void setIdentificador(int identificador) {
		this.id = identificador;
	}


	public int getDeviceID() {
		return deviceID;
	}


	public void setDeviceID(int deviceID) {
		this.deviceID = deviceID;
	}


	@Override
	public int hashCode() {
		return Objects.hash(deviceID, id, nombre, tipo);
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
		return deviceID == other.deviceID && id == other.id
				&& Objects.equals(nombre, other.nombre) && Objects.equals(tipo, other.tipo);
	}


	@Override
	public String toString() {
		return "Sensor [nombre=" + nombre + ", tipo=" + tipo + ", identificador=" + id + ", deviceID="
				+ deviceID + "]";
	}
	
	
		
		
}


