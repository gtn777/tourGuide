package tourGuide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.dto.NearbyAttractionsDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

public class TestTourGuideService {

	@Test
	public void getUserLocation() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).join();
		tourGuideService.tracker.stopTracking();
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}

	@Test
	public void addUser() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();

		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}

	@Test
	public void getAllUsers() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();

		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}

	@Test
	public void trackUser() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).join();

		tourGuideService.tracker.stopTracking();

		assertEquals(user.getUserId(), visitedLocation.userId);
	}

	@Test
	public void getNearbyAttractions() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user).join();

		NearbyAttractionsDto dto = tourGuideService.getNearbyAttractions(visitedLocation, user);

		tourGuideService.tracker.stopTracking();

		assertEquals(5, dto.getNearbyAttractionDtos().size());
	}

	@Test
	public void getTripDeals() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		tourGuideService.tracker.stopTracking();

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		List<Provider> providers = tourGuideService.getTripDeals(user);

		for (Provider p : providers) {
			System.out.println(p.name);
			System.out.println(p.price);
		}
		assertEquals(5, providers.size());
	}

	@Test
	public void getTripDeals_accordingUserPreferences() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		tourGuideService.tracker.stopTracking();

		User user1 = new User(UUID.randomUUID(), "jon1", "001", "jon1@tourGuide.com");
		user1.getUserPreferences().setNumberOfAdults(0);
		user1.getUserPreferences().setNumberOfChildren(0);
		User user2 = new User(UUID.randomUUID(), "jon2", "002", "jon2@tourGuide.com");
		user2.getUserPreferences().setNumberOfAdults(2);
		User user3 = new User(UUID.randomUUID(), "jon3", "003", "jon3@tourGuide.com");
		user3.getUserPreferences().setNumberOfChildren(2);

		List<Provider> providers1 = tourGuideService.getTripDeals(user1);
		List<Provider> providers2 = tourGuideService.getTripDeals(user2);
		List<Provider> providers3 = tourGuideService.getTripDeals(user3);

		assertTrue(providers2.get(0).price > providers1.get(0).price);
		assertTrue(providers3.get(0).price > providers1.get(0).price);
	}
}
