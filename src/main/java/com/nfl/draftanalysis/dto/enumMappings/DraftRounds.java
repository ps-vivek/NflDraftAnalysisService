package com.nfl.draftanalysis.dto.enumMappings;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum DraftRounds {
	ROUND_ONE(1), ROUND_TWO(2), ROUND_THREE(3), ROUND_FOUR(4), ROUND_FIVE(5), ROUND_SIX(6), ROUND_SEVEN(7);

	@Getter
	private int round;

	public static DraftRounds getDraftRoundEnum(int round) {
		for (DraftRounds draftRound : DraftRounds.values()) {
			if (draftRound.getRound() == round)
				return draftRound;
		}
		return null;
	}

}
