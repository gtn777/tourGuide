package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.time.StopWatch;
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
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;

	private final ExecutorService pool = Executors.newFixedThreadPool(50);

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

	public List<CompletableFuture<Void>> calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (Attraction attraction : attractions) {
			for (VisitedLocation visitedLocation : userLocations) {
				if (user.getUserRewards()
						.stream()
						.anyMatch(t -> t.attraction.attractionName == attraction.attractionName)) {
					break;
				} else if (nearAttraction(visitedLocation, attraction)) {
					CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
						user.getUserRewards()
								.add(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}, pool);
					futures.add(future);
					break;
				} else {
					continue;
				}
			}
		}
		return futures;
	}

	private int getRewardPointsCount = 0;
	private List<Double> durations = new ArrayList<>();

	public Double getDurationsAverage() {
		return durations.stream().mapToDouble(d -> d).average().orElse(0);
	}

	private Integer getRewardPoints(Attraction attraction, User user) {
		StopWatch watch = new StopWatch();
		watch.start();
		int points = rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
		watch.stop();
		this.incrementGetRewardPointsCount();
		durations.add((double) watch.getTime() / 1000);
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

	public int getGetRewardPointsCount() {
		return getRewardPointsCount;
	}

	private void setGetRewardPointsCount(int getRewardPointsCount) {
		this.getRewardPointsCount = getRewardPointsCount;
	}
	private void incrementGetRewardPointsCount() {
		this.setGetRewardPointsCount(this.getRewardPointsCount+1);
	}
}
