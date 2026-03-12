package com.asrevo.cvhome.uaa.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GeneralExceptionHandler {

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException e) {
		log.warn("Data integrity violation: {}", e.getMessage());
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
				"This resource cannot be deleted because it is in use by another entity.");
		problemDetail.setTitle("Resource in use");
		return problemDetail;
	}

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleException(Exception e) {
		log.error("Internal Server Error", e);
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				e.getMessage());
		problemDetail.setTitle("Internal Server Error");
		return problemDetail;
	}

}
