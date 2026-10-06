package com.kata.berlinclock.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.kata.berlinclock.clock.InvalidTimeException;

/**
 * Errors shared by every feature, as RFC 9457 problem details. Extending
 * {@link ResponseEntityExceptionHandler} gives Spring's own errors (such as a body that is not JSON)
 * the same shape. Errors that belong to one feature are handled in that feature's web package.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler
	ProblemDetail invalidTime(InvalidTimeException exception) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
		problem.setTitle("Invalid time");
		return problem;
	}
}
