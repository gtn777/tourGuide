package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {
	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
	private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 300;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;

	private final ExecutorService pool = Executors.newFixedThreadPool(200);

	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	public CompletableFuture<Void> calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();
		List<UserReward> userRewards = new ArrayList<>(user.getUserRewards());

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (Attraction attraction : attractions) {
			for (VisitedLocation visitedLocation : userLocations) {
				if (nearAttraction(visitedLocation, attraction)) {
					if (userRewards.stream().anyMatch(t -> t.attraction.attractionName == attraction.attractionName)) {
						break;
					} else {
						CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
							user.addUserReward(
									new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
						}, pool);
						futures.add(future);
						break;
					}
				} else {
					continue;
				}
			}
		}
		return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
	}

	protected Integer getRewardPoints(Attraction attraction, User user) {
		int points = rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
		return points;
	}

	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}

	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}

	public double getDistance(Location loc1, Location loc2) {
		double lat1 = Math.toRadians(loc1.latitude);
		double lon1 = Math.toRadians(loc1.longitude);
		double lat2 = Math.toRadians(loc2.latitude);
		double lon2 = Math.toRadians(loc2.longitude);

		double angle = Math
				.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		double nauticalMiles = 60 * Math.toDegrees(angle);
		double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
		return statuteMiles;
	}

	public Executor getPool() {
		return this.pool;
	}

}
