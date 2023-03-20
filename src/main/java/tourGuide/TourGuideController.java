package tourGuide;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.dto.GetUserRewardsDto;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;

	@GetMapping("/")
	public String index() {
		return "Greetings from TourGuide!";
	}

	@GetMapping("/getLocation")
	public String getLocation(@RequestParam String userName) {
		VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		return JsonStream.serialize(visitedLocation.location);
	}

	@GetMapping("/getNearbyAttractions")
	public String getNearbyAttractions(@RequestParam String userName) throws JsonProcessingException {
		VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(tourGuideService.getNearbyAttractions(visitedLocation, getUser(userName)));
		return json;
	}

	@GetMapping("/getRewards")
	public String getRewards(@RequestParam String userName) {
		return JsonStream.serialize(new GetUserRewardsDto(tourGuideService.getUserRewards(getUser(userName))));
	}

	@GetMapping("/getAllCurrentLocations")
	public String getAllCurrentLocations() {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json;
		try {
			json = ow.writeValueAsString(tourGuideService.getEveryUserMostRecentLocation());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "json error";
		}
		return json;
	}

	@GetMapping("/getTripDeals")
	public String getTripDeals(@RequestParam String userName) {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json;
		try {
			json = ow.writeValueAsString(tourGuideService.getTripDeals(getUser(userName)));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "json error";
		}
		return json;
	}

	private User getUser(String userName) {
		return tourGuideService.getUser(userName);
	}

}