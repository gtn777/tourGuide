package tourGuide.dto;

import gpsUtil.location.Location;

public class LocationDto {

	private double longitude;

	private double latitude;

	public LocationDto(Location loc) {
		super();
		this.longitude = loc.longitude;
		this.latitude = loc.latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

}
