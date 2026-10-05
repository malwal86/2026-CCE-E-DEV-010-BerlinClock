package com.kata.berlinclock.conversion.application;

import java.time.Instant;
import java.time.LocalTime;

import com.kata.berlinclock.clock.BerlinClock;

/**
 * A time someone converted, and when. The Berlin Clock is derived on demand, never stored.
 */
public record Conversion(long id, LocalTime time, Instant convertedAt) {

	public BerlinClock berlinClock() {
		return BerlinClock.of(time);
	}
}
