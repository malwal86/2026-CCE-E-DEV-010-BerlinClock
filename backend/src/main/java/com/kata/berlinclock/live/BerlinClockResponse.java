package com.kata.berlinclock.live;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.kata.berlinclock.clock.BerlinClock;


record BerlinClockResponse(String time, String clock, String seconds, String fiveHours, String singleHours,
		String fiveMinutes, String singleMinutes) {

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
