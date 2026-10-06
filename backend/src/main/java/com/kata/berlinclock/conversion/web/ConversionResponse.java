package com.kata.berlinclock.conversion.web;

import java.time.Instant;

import com.kata.berlinclock.clock.DigitalTime;
import com.kata.berlinclock.conversion.application.Conversion;


record ConversionResponse(long id, String time, Instant convertedAt, String clock, String seconds, String fiveHours,
		String singleHours, String fiveMinutes, String singleMinutes) {

	static ConversionResponse from(Conversion conversion) {
		var berlinClock = conversion.berlinClock();
		return new ConversionResponse(
				conversion.id(),
				DigitalTime.format(conversion.time()),
				conversion.convertedAt(),
				berlinClock.code(),
				String.valueOf(berlinClock.seconds().symbol()),
				berlinClock.fiveHours().notation(),
				berlinClock.singleHours().notation(),
				berlinClock.fiveMinutes().notation(),
				berlinClock.singleMinutes().notation());
	}
}
