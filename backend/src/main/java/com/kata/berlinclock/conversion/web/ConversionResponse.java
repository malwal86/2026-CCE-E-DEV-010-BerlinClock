package com.kata.berlinclock.conversion.web;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import com.kata.berlinclock.conversion.application.Conversion;

import io.swagger.v3.oas.annotations.media.Schema;

/** A saved conversion. The Berlin Clock fields are computed from the time every time it is read. */
record ConversionResponse(
		@Schema(description = "Id of the conversion, also the end of its Location", example = "12")
		long id,
		@Schema(description = "The time converted, HH:mm:ss", example = "16:50:06")
		String time,
		@Schema(description = "When it was converted, UTC, ISO-8601", example = "2026-10-05T14:03:12Z")
		Instant convertedAt,
		@Schema(description = "The entire clock, 24 lamps from top to bottom (Y yellow, R red, O off)",
				example = "YRRROROOOYYRYYRYYRYOOOOO")
		String clock,
		@Schema(description = "Seconds lamp: Y on even seconds, O on odd ones", example = "Y")
		String seconds,
		@Schema(description = "Five-hours row, 4 lamps, each lit lamp is 5 hours", example = "RRRO")
		String fiveHours,
		@Schema(description = "Single-hours row, 4 lamps, each lit lamp is 1 hour", example = "ROOO")
		String singleHours,
		@Schema(description = "Five-minutes row, 11 lamps, each lit lamp is 5 minutes, R marks the quarters",
				example = "YYRYYRYYRYO")
		String fiveMinutes,
		@Schema(description = "Single-minutes row, 4 lamps, each lit lamp is 1 minute", example = "OOOO")
		String singleMinutes) {

	private static final DateTimeFormatter DIGITAL_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

	static ConversionResponse from(Conversion conversion) {
		var berlinClock = conversion.berlinClock();
		return new ConversionResponse(
				conversion.id(),
				DIGITAL_TIME.format(conversion.time()),
				conversion.convertedAt(),
				berlinClock.code(),
				String.valueOf(berlinClock.seconds().symbol()),
				berlinClock.fiveHours().notation(),
				berlinClock.singleHours().notation(),
				berlinClock.fiveMinutes().notation(),
				berlinClock.singleMinutes().notation());
	}
}
