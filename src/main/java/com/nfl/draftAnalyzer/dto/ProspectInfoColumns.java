package com.nfl.draftAnalyzer.dto;

public enum ProspectInfoColumns {

	PLAYER(0), COLLEGE(1), STATUS(2), POSITION(3), TEAM(4), CLASS(5), GRADE(6);

	private int val;

	private ProspectInfoColumns(int value) {
		val = value;
	}

	public int getValue() {
		return val;
	}
}
