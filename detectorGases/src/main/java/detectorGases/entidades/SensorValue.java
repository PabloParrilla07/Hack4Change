package detectorGases.entidades;

import java.util.Objects;

public class SensorValue{
	//MQ-2, MQ-9 y MICS5524
	//id (PK), id_sensor (FK), valor (float), timestamp (datetime)
	protected Integer sensorValueId;//(PK)
	protected Integer sensorId;//(FK) Id de la clase "Sensor"
	protected Float value;
	protected Long timestamp;
	
	public SensorValue(Integer id, Integer IdSensor, Float value, Long timestamp) {
		super();
		this.sensorValueId = id;
		this.sensorId=IdSensor;
		this.value = value;
		this.timestamp=timestamp;
	}

	public Integer getId() {
		return sensorValueId;
	}

	public void setId(Integer id) {
		sensorValueId = id;
	}

	public Integer getIdSensor() {
		return sensorId;
	}

	public void setIdSensor(Integer idSensor) {
		sensorId = idSensor;
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
		return Objects.hash(sensorValueId, sensorId, timestamp, value);
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
		return Objects.equals(sensorValueId, other.sensorValueId) && Objects.equals(sensorId, other.sensorId)
				&& Objects.equals(timestamp, other.timestamp) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "SensorValue [Id=" + sensorValueId + ", IdSensor=" + sensorId + ", value=" + value + ", timestamp=" + timestamp
				+ "]";
	}
	
}
