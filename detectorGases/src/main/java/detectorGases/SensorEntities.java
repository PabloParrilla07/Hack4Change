package detectorGases;

import java.util.Objects;

public class SensorEntities {

	protected long timestamp;
	protected float value;
	protected String type;
	protected int deviceID;

		
	public SensorEntities(long timestamp, float value, String type, int deviceID) {
		super();
		this.timestamp = timestamp;
		this.value = value;
		this.type = type;
		this.deviceID = deviceID;
	}


	public long getTimestamp() {
		return timestamp;
	}


	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}


	public float getValue() {
		return value;
	}


	public void setValue(float value) {
		this.value = value;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public int getDeviceID() {
		return deviceID;
	}


	public void setDeviceID(int deviceID) {
		this.deviceID = deviceID;
	}


		@Override
	public int hashCode() {
		return Objects.hash(deviceID, timestamp, type, value);
	}


		@Override
	public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SensorEntities other = (SensorEntities) obj;
			return deviceID == other.deviceID && timestamp == other.timestamp && Objects.equals(type, other.type)
					&& Float.floatToIntBits(value) == Float.floatToIntBits(other.value);
		}


		@Override
	public String toString() {
			return "Sensor [timestamp=" + timestamp + ", value=" + value + ", type=" + type + ", deviceID=" + deviceID
					+ "]";
	}
		
		
		
		
}


