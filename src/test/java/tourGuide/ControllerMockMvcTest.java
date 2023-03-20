package tourGuide;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerMockMvcTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	public void indexShouldReturnDefaultMessage() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Greetings from TourGuide!")));
	}

	@Test
	public void getLocation_shouldReturnLocation() throws Exception {
		mockMvc.perform(get("/getLocation?userName=internalUser2"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("latitude")))
				.andExpect(content().string(containsString("longitude")));
	}

	@Test
	public void getNearbyAttractions_shouldReturnDto() throws Exception {
		mockMvc.perform(get("/getNearbyAttractions?userName=internalUser2"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("attractions")))
				.andExpect(content().string(containsString("userLocation")));
	}

	@Test
	public void getRewards_shouldReturnRewardsDto() throws Exception {
		mockMvc.perform(get("/getRewards?userName=internalUser2"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("rewards")));
	}

	@Test
	public void getAllCurrentLocations_shouldReturnMappingUserLocation() throws Exception {
		mockMvc.perform(get("/getAllCurrentLocations"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("latitude")))
				.andExpect(content().string(containsString("longitude")));
	}

	@Test
	public void getTripDeals_shouldReturnMappingUserLocation() throws Exception {
		mockMvc.perform(get("/getTripDeals?userName=internalUser2"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("price")))
				.andExpect(content().string(containsString("tripId")));
	}

}
