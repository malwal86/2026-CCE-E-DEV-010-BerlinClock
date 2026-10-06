package com.kata.berlinclock.conversion.web;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import com.kata.berlinclock.conversion.application.Conversion;


record ConversionResponse(long id, String time, Instant convertedAt, String seconds, String fiveHours) {

	private static final DateTimeFormatter DIGITAL_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

	static ConversionResponse from(Conversion conversion) {
		var berlinClock = conversion.berlinClock();
		return new ConversionResponse(
				conversion.id(),
				DIGITAL_TIME.format(conversion.time()),
				conversion.convertedAt(),
				String.valueOf(berlinClock.seconds().symbol()),
				berlinClock.fiveHours().notation());
	}
}
