package com.kata.berlinclock.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.kata.berlinclock.clock.InvalidTimeException;

/**
 * Errors shared by every feature, as RFC 9457 problem details. Extending
 * {@link ResponseEntityExceptionHandler} gives Spring's own errors (such as a body that is not JSON)
 * the same shape, and any other exception becomes a 500 without internals. Errors that belong to one feature are
 * handled in that feature's web package.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	@ExceptionHandler
	ProblemDetail invalidTime(InvalidTimeException exception) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
		problem.setTitle("Invalid time");
		return problem;
	}

	/** Anything else is a bug: logged here, while the client gets a problem detail with no internals in it. */
	@ExceptionHandler
	ProblemDetail unexpected(Exception exception) {
		log.error("Unexpected error", exception);
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
				"Something went wrong. Please try again later.");
		problem.setTitle("Unexpected error");
		return problem;
	}
}
