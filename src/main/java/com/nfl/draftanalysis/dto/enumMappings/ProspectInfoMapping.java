package com.nfl.draftanalysis.dto.enumMappings;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ProspectInfoMapping {

	PLAYER(0), COLLEGE(1), STATUS(2), POSITION(3), TEAM(4), CLASS(5), GRADE(6), CONFERENCE(7);

	@Getter
	private int value;

}
