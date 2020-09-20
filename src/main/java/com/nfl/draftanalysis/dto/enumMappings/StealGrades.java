package com.nfl.draftanalysis.dto.enumMappings;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum StealGrades {
	MINUS_TWO_POINT_FIVE(-2.5), MINUS_TWO(-2), MINUS_ONE_POINT_FIVE(-1.5), MINUS_ONE(-1.0),MINUS_POINT_FIVE(-0.5), ZERO(0),
	PLUS_THREE_POINT_FIVE(2.5), PLUS_THREE(3), PLUS_TWO_POINT_FIVE(2.5), PLUS_TWO(2), PLUS_ONE_POINT_FIVE(1.5),
	PLUS_ONE(1.0), PLUS_POINT_FIVE(0.5);
	;

	@Getter
	private double stealMultiplier;

}
