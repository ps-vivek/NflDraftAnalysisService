package com.nfl.draftanalysis.dto.enumMappings;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum NflProspectTiers {
	TIER_ONE_PROSPECTS(6.9), TIER_TWO_PROSPECTS(6.5), TIER_THREE_PROSPECTS(6.1), TIER_FOUR_PROSPECTS(5.5),
	TIER_FIVE_PROSPECTS(5);

	@Getter
	private double value;
}
