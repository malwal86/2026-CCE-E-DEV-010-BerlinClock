package com.kata.berlinclock.conversion.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * No conversion has the requested id. As an {@link ErrorResponseException} it carries its own problem detail,
 * which Spring renders as {@code application/problem+json} with no handler of its own.
 */
class ConversionNotFoundException extends ErrorResponseException {

	ConversionNotFoundException(long id) {
		super(HttpStatus.NOT_FOUND, problem(id), null);
	}

	private static ProblemDetail problem(long id) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Conversion %d not found".formatted(id));
		problem.setTitle("Conversion not found");
		return problem;
	}
}
