package tourGuide;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import gpsUtil.GpsUtil;
import rewardCentral.RewardCentral;
import tourGuide.service.RewardsService;

@Configuration
public class TourGuideModule {

	@Bean
	GpsUtil getGpsUtil() {
		return new GpsUtil();
	}

	@Bean
	RewardsService getRewardsService() {
		return new RewardsService(getGpsUtil(), getRewardCentral());
	}

	@Bean
	RewardCentral getRewardCentral() {
		return new RewardCentral();
	}

}
