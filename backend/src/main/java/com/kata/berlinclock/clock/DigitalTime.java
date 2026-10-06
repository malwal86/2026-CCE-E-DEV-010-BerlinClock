package com.kata.berlinclock.clock;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * The time format shared by every story: a strict HH:mm:ss time of day, 00:00:00 to 23:59:59.
 */
public final class DigitalTime {

	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
			.withResolverStyle(ResolverStyle.STRICT);

	private DigitalTime() {
	}

	public static LocalTime parse(String text) {
		if (text == null || text.isBlank()) {
			throw new InvalidTimeException("A time is required (HH:mm:ss)");
		}
		try {
			return LocalTime.parse(text, FORMAT);
		}
		catch (DateTimeParseException e) {
			throw new InvalidTimeException(
					"Invalid time '%s': expected HH:mm:ss between 00:00:00 and 23:59:59".formatted(text));
		}
	}

	/** Always with its seconds, unlike {@link LocalTime#toString()}, which writes 12:00:00 as 12:00. */
	public static String format(LocalTime time) {
		return FORMAT.format(time);
	}
}
