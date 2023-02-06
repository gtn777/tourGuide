package tourGuide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

public class TestPerformance {

	/*
	 * A note on performance improvements:
	 * 
	 * The number of users generated for the high volume tests can be easily
	 * adjusted via this method:
	 * 
	 * InternalTestHelper.setInternalUserNumber(100000);
	 * 
	 * 
	 * These tests can be modified to suit new solutions, just as long as the
	 * performance metrics at the end of the tests remains consistent.
	 * 
	 * These are performance metrics that we are trying to hit:
	 * 
	 * highVolumeTrackLocation: 100,000 users within 15 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 *
	 * highVolumeGetRewards: 100,000 users within 20 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	private final ExecutorService pool = Executors.newFixedThreadPool(20);

	private CompletableFuture<VisitedLocation> getAsyncUserLocation(User user, TourGuideService tourGuideService) {
		return CompletableFuture.supplyAsync(() -> tourGuideService.trackUserLocation(user), pool);
	}

//	@Disabled
	@Test
	public void highVolumeTrackLocation() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15
		// minutes
		InternalTestHelper.setInternalUserNumber(200);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		StopWatch stopWatch = new StopWatch();

		stopWatch.start();

		List<CompletableFuture<VisitedLocation>> completableFutures = allUsers.stream()
				.map(user -> getAsyncUserLocation(user, tourGuideService)).collect(Collectors.toList());

		CompletableFuture<Void> allFuture = CompletableFuture
				.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]));

		CompletableFuture<List<VisitedLocation>> allLocationListFuture = allFuture.thenApply(future -> {
			return completableFutures.stream().map(completableFuture -> completableFuture.join())
					.collect(Collectors.toList());
		});

		CompletableFuture<List<String>> allLocationStringListFuture = allLocationListFuture.thenApply(list -> {
			return list.stream().map((l) -> l.toString()).collect(Collectors.toList());
		});

		List<String> locationStringList = allLocationStringListFuture.join();

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: "
				+ TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
		assertTrue(locationStringList.size() == InternalTestHelper.getInternalUserNumber());// ---------------------
	}

	@Disabled
	@Test
	public void highVolumeGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		// Users should be incremented up to 100,000, and test finishes within 20
		// minutes
		InternalTestHelper.setInternalUserNumber(100);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);

		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		allUsers.forEach(u -> rewardsService.calculateRewards(u));

		for (User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
				+ " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

}
