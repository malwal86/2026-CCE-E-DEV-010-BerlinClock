package com.kata.berlinclock.live;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.kata.berlinclock.clock.BerlinClock;

import io.swagger.v3.oas.annotations.media.Schema;

/** A time shown as a Berlin Clock, not saved. */
record BerlinClockResponse(
		@Schema(description = "The time shown, HH:mm:ss", example = "16:50:06")
		String time,
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

	static BerlinClockResponse of(LocalTime time) {
		var berlinClock = BerlinClock.of(time);
		return new BerlinClockResponse(
				DIGITAL_TIME.format(time),
				berlinClock.code(),
				String.valueOf(berlinClock.seconds().symbol()),
				berlinClock.fiveHours().notation(),
				berlinClock.singleHours().notation(),
				berlinClock.fiveMinutes().notation(),
				berlinClock.singleMinutes().notation());
	}
}
