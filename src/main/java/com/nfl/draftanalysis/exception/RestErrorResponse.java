package com.nfl.draftanalysis.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RestErrorResponse {
	
	private String message;
	
	private HttpStatus httpCode;
	
	private LocalDateTime failedDateTime;

}
