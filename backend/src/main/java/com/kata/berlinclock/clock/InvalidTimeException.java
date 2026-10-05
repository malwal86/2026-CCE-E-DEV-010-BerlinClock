package com.kata.berlinclock.clock;

/**
 * The text given is not a time the Berlin Clock can show. The message is fit to show the user.
 */
public class InvalidTimeException extends RuntimeException {

	InvalidTimeException(String message) {
		super(message);
	}
}
