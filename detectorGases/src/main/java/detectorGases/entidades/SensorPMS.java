package detectorGases.entidades;

import java.util.Objects;

public class SensorPMS {
	
	//Nos interesa sacar el PM1 y el PM2.5
	
	protected Integer Id;//(PK)
	protected Integer IdSensor;//(FK) Id de la clase "Sensor"
	protected Float valuePM10;
	protected Float valuePM1;
	protected Float valuePM25;
	protected Long timestamp;
	
	public SensorPMS(Integer id, Integer IdSensor, Float valuePM10, Float valuePM1, Float valuePM25, Long timestamp) {
		super();
		this.Id = id;
		this.IdSensor=IdSensor;
		this.valuePM10 = valuePM10;
		this.valuePM1 = valuePM1;
		this.valuePM25 = valuePM25;
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

	public Float getValuePM10() {
		return valuePM10;
	}

	public void setValuePM10(Float valuePM10) {
		this.valuePM10 = valuePM10;
	}

	public Float getValuePM1() {
		return valuePM1;
	}

	public void setValuePM1(Float valuePM1) {
		this.valuePM1 = valuePM1;
	}

	public Float getValuePM25() {
		return valuePM25;
	}

	public void setValuePM25(Float valuePM25) {
		this.valuePM25 = valuePM25;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Id, IdSensor, timestamp, valuePM1, valuePM10, valuePM25);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SensorPMS other = (SensorPMS) obj;
		return Objects.equals(Id, other.Id) && Objects.equals(IdSensor, other.IdSensor)
				&& Objects.equals(timestamp, other.timestamp) && Objects.equals(valuePM1, other.valuePM1)
				&& Objects.equals(valuePM10, other.valuePM10) && Objects.equals(valuePM25, other.valuePM25);
	}

	@Override
	public String toString() {
		return "SensorPMS [Id=" + Id + ", IdSensor=" + IdSensor + ", valuePM10=" + valuePM10 + ", valuePM1=" + valuePM1
				+ ", valuePM25=" + valuePM25 + ", timestamp=" + timestamp + "]";
	}

}
