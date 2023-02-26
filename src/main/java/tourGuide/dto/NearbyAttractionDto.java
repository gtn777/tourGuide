package tourGuide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NearbyAttractionDto {
	private String name;
	private double distance;
	private Integer reward;
	private double latitude;
	private double longitude;

	public NearbyAttractionDto(String name, double distance, Integer reward, double latitude, double longitude) {
		super();
		this.name = name;
		this.distance = distance;
		this.reward = reward;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public Integer getReward() {
		return reward;
	}

	public void setReward(Integer reward) {
		this.reward = reward;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

}
