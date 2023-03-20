package tourGuide.dto;

import lombok.Data;

@Data
public class AttractionDto {
	private String name;
	private double distance;
	private Integer reward;
	private double latitude;
	private double longitude;

	public AttractionDto(String name, double distance, Integer reward, double latitude, double longitude) {
		super();
		this.name = name;
		this.distance = distance;
		this.reward = reward;
		this.latitude = latitude;
		this.longitude = longitude;
	}

}
