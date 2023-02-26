package tourGuide.dto;

import java.util.List;
import java.util.stream.Collectors;

import tourGuide.user.UserReward;

public class GetUserRewardsDto {

	public GetUserRewardsDto(List<UserReward> userRewards) {
		rewards = userRewards.stream()
				.map(ur -> ur.attraction.attractionName + ": " + ur.getRewardPoints() + " points.")
				.collect(Collectors.toList());
	}

	List<String> rewards;

	public List<String> getRewards() {
		return rewards;
	}

	public void setRewards(List<String> rewards) {
		this.rewards = rewards;
	}
}
