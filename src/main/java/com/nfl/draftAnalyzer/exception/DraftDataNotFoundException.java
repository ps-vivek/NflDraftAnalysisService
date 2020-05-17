package com.nfl.draftAnalyzer.exception;

public class DraftDataNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DraftDataNotFoundException(String message) {
		super(message);
	}

}
