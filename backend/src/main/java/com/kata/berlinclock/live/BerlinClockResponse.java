package com.kata.berlinclock.live;

import java.time.LocalTime;

import com.kata.berlinclock.clock.BerlinClock;
import com.kata.berlinclock.clock.DigitalTime;


record BerlinClockResponse(String time, String clock, String seconds, String fiveHours, String singleHours,
		String fiveMinutes, String singleMinutes) {

	static BerlinClockResponse of(LocalTime time) {
		var berlinClock = BerlinClock.of(time);
		return new BerlinClockResponse(
				DigitalTime.format(time),
				berlinClock.code(),
				String.valueOf(berlinClock.seconds().symbol()),
				berlinClock.fiveHours().notation(),
				berlinClock.singleHours().notation(),
				berlinClock.fiveMinutes().notation(),
				berlinClock.singleMinutes().notation());
	}
}
