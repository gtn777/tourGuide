package tourGuide.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import gpsUtil.location.Location;

public class GetNearbyAttractionsDto {

	private LocationDto userLocation;

	@JsonProperty("attractions")
	List<NearbyAttractionDto> nearByAttractionDtos;

	public GetNearbyAttractionsDto(Location loc, List<NearbyAttractionDto> nearByAttractionDtos) {
		super();
		this.userLocation = new LocationDto(loc);
		this.nearByAttractionDtos = nearByAttractionDtos;
	}

	public LocationDto getUserLocation() {
		return userLocation;
	}

	public void setUserLocation(LocationDto userLocation) {
		this.userLocation = userLocation;
	}

	public List<NearbyAttractionDto> getNearByAttractionDtos() {
		return nearByAttractionDtos;
	}

	public void setNearByAttractionDtos(List<NearbyAttractionDto> nearByAttractionDtos) {
		this.nearByAttractionDtos = nearByAttractionDtos;
	}

}
