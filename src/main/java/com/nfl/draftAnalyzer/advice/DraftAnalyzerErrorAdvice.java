package com.nfl.draftAnalyzer.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.nfl.draftAnalyzer.exception.DraftDataNotFoundException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class DraftAnalyzerErrorAdvice {

	@ExceptionHandler({ DraftDataNotFoundException.class })
	public ResponseEntity<String> handleNotFoundException(DraftDataNotFoundException e) {
		return error(HttpStatus.NOT_FOUND, e);
	}

	private ResponseEntity<String> error(HttpStatus status, Exception e) {
		log.error("Exception : ", e);
		return ResponseEntity.status(status).body(e.getMessage());
	}
}
