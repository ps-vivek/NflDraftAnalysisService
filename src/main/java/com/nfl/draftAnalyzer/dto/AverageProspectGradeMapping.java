package com.nfl.draftAnalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum AverageProspectGradeMapping {
	TEAM_NAME("teamName"), 
	NO_OF_PLAYERS_DRAFTED("noOfPlayersDrafted"), 
	AVERAGE_GRADE("averageGrade"),
	PLAYERS_DRAFTED("playersDrafted");

	@Getter
	private String value;

}
