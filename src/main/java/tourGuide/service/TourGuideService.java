package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.dto.AttractionDto;
import tourGuide.dto.LocationDto;
import tourGuide.dto.NearbyAttractionsDto;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	private final ExecutorService pool = Executors.newFixedThreadPool(30);

	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user).join();
		return visitedLocation;
	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();

		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
		CompletableFuture<VisitedLocation> visitedLocationCompletableFuture = CompletableFuture.supplyAsync(() -> {
			VisitedLocation loc = gpsUtil.getUserLocation(user.getUserId());
			return loc;
		}, pool).thenApplyAsync((loc) -> {
			user.addToVisitedLocations(loc);
			rewardsService.calculateRewards(user).join();
			return loc;
		}, rewardsService.getPool());
		return visitedLocationCompletableFuture;
	}

	public CompletableFuture<Void> trackAllUserLocation(List<User> users) {
		List<CompletableFuture<VisitedLocation>> completableFutures = users.stream()
				.map(user -> this.trackUserLocation(user))
				.collect(Collectors.toList());
		return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));
	}

	public NearbyAttractionsDto getNearbyAttractions(VisitedLocation visitedLocation, User user) {
		List<Attraction> allAttractions = new ArrayList<>(gpsUtil.getAttractions());
		List<Double> allDistances = new ArrayList<>(allAttractions.stream()
				.map(a -> rewardsService.getDistance(a, visitedLocation.location))
				.collect(Collectors.toList()));
		List<AttractionDto> attractionDtos = new ArrayList<>();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			for (Attraction a : allAttractions) {
				double distance = rewardsService.getDistance(a, visitedLocation.location);
				if (distance == Collections.min(allDistances)) {
					futures.add(CompletableFuture.supplyAsync(() -> {
						attractionDtos.add(new AttractionDto(a.attractionName,
								rewardsService.getDistance(a, visitedLocation.location),
								rewardsService.getRewardPoints(a, user), a.latitude, a.longitude));
						return null;
					}, rewardsService.getPool()));
					allDistances.remove(distance);
					allAttractions.remove(a);
					break;
				} else {
					continue;
				}
			}
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
		return new NearbyAttractionsDto(visitedLocation.location, attractionDtos);
	}

	public Map<String, LocationDto> getEveryUserMostRecentLocation() {
		Map<String, LocationDto> maps = new HashMap<>();
		List<User> allUsers = getAllUsers();
		for (User u : allUsers) {
			maps.put(u.getUserId().toString(), new LocationDto(getUserLocation(u).location));
		}
		return maps;
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
					new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
}
