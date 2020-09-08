package com.nfl.draftanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class GradeBreakDown {

	private Double stealIndex;

	private Double prospectGradeWithStealIndex;
	
	private Double prospectGradeWithoutStealIndex;
	
	private String playerName;
}
