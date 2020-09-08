package com.nfl.draftanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum OverallTeamStandingsMappings {

	SEED(0), TEAM(1), WINS(2), LOSS(3), TIES(4), POSITION(5), REASON(6), CONFERENCE(7), YEAR(8);

	@Getter
	private int value;

}
