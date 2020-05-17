package com.nfl.draftAnalyzer.advice;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.nfl.draftAnalyzer.exception.DraftDataNotFoundException;
import com.nfl.draftAnalyzer.exception.ExcelReadException;
import com.nfl.draftAnalyzer.exception.ExcelWriteException;
import com.nfl.draftAnalyzer.exception.InvalidNflTeamException;
import com.nfl.draftAnalyzer.exception.RestErrorResponse;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class DraftAnalyzerErrorAdvice {

	@ExceptionHandler({ DraftDataNotFoundException.class })
	public ResponseEntity<RestErrorResponse> draftDataNotFoundError(DraftDataNotFoundException e) {
		return handleError(HttpStatus.NOT_FOUND, e);
	}

	@ExceptionHandler({ ExcelReadException.class })
	public ResponseEntity<RestErrorResponse> excelReadExceptionError(ExcelReadException e) {
		return handleError(HttpStatus.INTERNAL_SERVER_ERROR, e);
	}

	@ExceptionHandler({ ExcelWriteException.class })
	public ResponseEntity<RestErrorResponse> excelWriteExceptionError(ExcelWriteException e) {
		return handleError(HttpStatus.INTERNAL_SERVER_ERROR, e);
	}

	@ExceptionHandler({ RuntimeException.class })
	public ResponseEntity<RestErrorResponse> handleRunTimeException(RuntimeException e) {
		return handleError(HttpStatus.INTERNAL_SERVER_ERROR, e);
	}

	@ExceptionHandler({ InvalidNflTeamException.class })
	public ResponseEntity<RestErrorResponse> handleInvalidNflTeamException(InvalidNflTeamException e) {
		return handleError(HttpStatus.BAD_REQUEST, e);
	}

	private ResponseEntity<RestErrorResponse> handleError(HttpStatus status, Exception e) {
		log.error("Exception : ", e);
		return ResponseEntity.status(status)
				.body(new RestErrorResponse(e.getLocalizedMessage(), status, LocalDateTime.now()));
	}
}
