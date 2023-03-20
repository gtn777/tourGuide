package tourGuide.dto;

import gpsUtil.location.Location;
import lombok.Data;

@Data
public class LocationDto {

	private double longitude;

	private double latitude;

	public LocationDto(Location loc) {
		super();
		this.longitude = loc.longitude;
		this.latitude = loc.latitude;
	}

}
