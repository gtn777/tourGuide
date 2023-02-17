package tourGuide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

public class TestPerformance {

	private Logger logger = LoggerFactory.getLogger(TestPerformance.class);

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

//	@Disabled
	@Test
	public void highVolumeTrackLocation() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
//		 Users should be incremented up to 100,000, and test finishes within 15
		// minutes
		InternalTestHelper.setInternalUserNumber(1040);
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		tourGuideService.tracker.stopTracking();
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		StopWatch stopWatch = new StopWatch();

		stopWatch.start();
		List<VisitedLocation> resultList = new ArrayList<>();
		try {
			resultList = tourGuideService.trackAllUserLocation(allUsers).get();
		} catch (InterruptedException | ExecutionException e1) {
			logger.error(e1.getMessage());
		}

		stopWatch.stop();
		System.out.println("              ------------             ");
		System.out.println("              ------------             ");
		System.out.println("highVolumeTrackLocation: Time Elapsed: "
				+ TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));

		assertTrue(resultList.size() == InternalTestHelper.getInternalUserNumber());// ---------------------
		
		System.out.println("highVolumeGetRewards " + "user:" + allUsers.size() + " Time: "
				+ ((double) stopWatch.getTime() / 1000));
		
		System.out.println("-  GpsUtil longProcess: " + tourGuideService.getGpsUtilLongCount());
		System.out.println("-  gpsutil average duration : " + tourGuideService.getDurationsAverage());
		System.out.println("-  RewardCenter longProcess: " + rewardsService.getGetRewardPointsCount());
		System.out.println("-  longProcess average duration : " + rewardsService.getDurationsAverage());
		System.out.println("------------------------------------------------------");
	}

//	@Disabled
	@Test
	public void highVolumeGetRewards() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
//		rewardsService.setProximityBuffer(0);
		// Users should be incremented up to 100,000, and test finishes within 20
		// minutes
		InternalTestHelper.setInternalUserNumber(40);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
		tourGuideService.tracker.stopTracking();

		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(
				u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		// --- WHEN --------------
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (User user : allUsers) {
			for (CompletableFuture<Void> future : rewardsService.calculateRewards(user)) {
				futures.add(future);
			}
		}
		System.out.println("-----------------------------------------------------");
		System.out.println(" - list des futures en " + ((double) stopWatch.getTime() / 1000) + " seconds.");
		CompletableFuture<Void> future = CompletableFuture
				.allOf(futures.toArray(new CompletableFuture[futures.size()]));
		System.out.println(" - future final en " + ((double) stopWatch.getTime() / 1000) + " seconds.");
		future.join();
		System.out.println(" - consommé en " + ((double) stopWatch.getTime() / 1000) + " seconds.");

		// --- THEN
		for (User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
		System.out.println("highVolumeGetRewards " + "user:" + allUsers.size() + " Time: "
				+ ((double) stopWatch.getTime() / 1000));
		
		System.out.println("-  RewardCenter longProcess: " + rewardsService.getGetRewardPointsCount());
		System.out.println("-  longProcess average duration : " + rewardsService.getDurationsAverage());
		System.out.println("------------------------------------------------------");
		
		
//		System.out.println("highVolumeGetRewards: Time Elapsed: "
//				+ TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
	}
}
