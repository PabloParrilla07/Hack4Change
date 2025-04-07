package detectorGases.entidades;

import java.util.Objects;

public class SensorValue{
	//MQ-2, MQ-9 y MICS5524
	//id (PK), id_sensor (FK), valor (float), timestamp (datetime)
	protected Integer Id;//(PK)
	protected Integer IdSensor;//(FK) Id de la clase "Sensor"
	protected Float value;
	protected Long timestamp;
	
	public SensorValue(Integer id, Integer IdSensor, Float value, Long timestamp) {
		super();
		this.Id = id;
		this.IdSensor=IdSensor;
		this.value = value;
		this.timestamp=timestamp;
	}

	public Integer getId() {
		return Id;
	}

	public void setId(Integer id) {
		Id = id;
	}

	public Integer getIdSensor() {
		return IdSensor;
	}

	public void setIdSensor(Integer idSensor) {
		IdSensor = idSensor;
	}

	public Float getValue() {
		return value;
	}

	public void setValue(Float value) {
		this.value = value;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Id, IdSensor, timestamp, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorValue other = (SensorValue) obj;
		return Objects.equals(Id, other.Id) && Objects.equals(IdSensor, other.IdSensor)
				&& Objects.equals(timestamp, other.timestamp) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "SensorValue [Id=" + Id + ", IdSensor=" + IdSensor + ", value=" + value + ", timestamp=" + timestamp
				+ "]";
	}
	
}
