package tourGuide.dto;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import tourGuide.user.UserReward;

@Data
public class GetUserRewardsDto {

	public GetUserRewardsDto(List<UserReward> userRewards) {
		rewards = userRewards.stream()
				.map(ur -> ur.attraction.attractionName + ": " + ur.getRewardPoints() + " points.")
				.collect(Collectors.toList());
	}

	List<String> rewards;

}
