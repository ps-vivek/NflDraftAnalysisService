package com.nfl.draftAnalyzer.exception;

public class InvalidNflTeamException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidNflTeamException(String message) {
		super(message);
	}

}
