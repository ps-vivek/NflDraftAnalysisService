package com.nfl.draftAnalyzer.dto;

import com.opencsv.bean.CsvBindByName;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ProspectInfo {

	public ProspectInfo(int noOfPlayers, String teamName, Double averageGrade, String playersDrafted) {
		super();
		this.noOfPlayersDrafted = noOfPlayers;
		this.teamName = teamName;
		this.averageGrade = averageGrade;
		this.playersDrafted = playersDrafted;
	}

	// @CsvBindByPosition(position = 1)
	@CsvBindByName(column = "No Of Players Drafted")
	private int noOfPlayersDrafted;

	@CsvBindByName(column = "Team Name")
	// @CsvBindByPosition(position = 0)
	private String teamName;

	// @CsvBindByPosition(position = 2)
	@CsvBindByName(column = "Average Grade")
	private Double averageGrade;

	// @CsvBindByPosition(position = 3)
	@CsvBindByName(column = "Players Drafted")
	private String playersDrafted;
}
