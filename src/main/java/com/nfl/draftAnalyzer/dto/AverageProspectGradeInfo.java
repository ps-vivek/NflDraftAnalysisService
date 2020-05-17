package com.nfl.draftAnalyzer.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class AverageProspectGradeInfo {

	public AverageProspectGradeInfo(int noOfPlayers, String teamName, Double averageGrade, String playersDrafted) {
		super();
		this.noOfPlayersDrafted = noOfPlayers;
		this.teamName = teamName;
		this.averageGrade = averageGrade;
		this.playersDrafted = playersDrafted;
	}

	private int noOfPlayersDrafted;

	private String teamName;

	private Double averageGrade;

	private String playersDrafted;
}
