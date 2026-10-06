package com.kata.berlinclock.conversion.web;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * The history cannot be reached (typically the database is down): 503, so the reader knows to try again later, and a
 * conversion that could not be saved says so. Runs before the shared handler, whose catch-all would otherwise answer 500.
 */
@RestControllerAdvice(assignableTypes = ConversionController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class HistoryUnavailableHandler {

	@ExceptionHandler
	ProblemDetail historyUnavailable(DataAccessException exception, HttpServletRequest request) {
		var detail = HttpMethod.POST.matches(request.getMethod())
				? "History is temporarily unavailable. Your conversion was not saved."
				: "History is temporarily unavailable.";
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, detail);
		problem.setTitle("History unavailable");
		return problem;
	}
}
