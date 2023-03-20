package tourGuide.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import gpsUtil.location.Location;
import lombok.Data;

@Data
public class NearbyAttractionsDto {

	private LocationDto userLocation;

	@JsonProperty("attractions")
	List<AttractionDto> nearbyAttractionDtos;

	public NearbyAttractionsDto(Location loc, List<AttractionDto> nearByAttractionDtos) {
		super();
		this.userLocation = new LocationDto(loc);
		this.nearbyAttractionDtos = nearByAttractionDtos;
	}

}
