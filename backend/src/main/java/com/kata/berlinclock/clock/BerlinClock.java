package com.kata.berlinclock.clock;

import java.time.LocalTime;

public final class BerlinClock {

	private final LocalTime time;

	private BerlinClock(LocalTime time) {
		this.time = time;
	}

	public static BerlinClock of(LocalTime time) {
		return new BerlinClock(time);
	}

	public Lamp seconds() {
		return time.getSecond() % 2 == 0 ? Lamp.YELLOW : Lamp.OFF;
	}

	/** Four red lamps, one lit per full five hours. */
	public LampRow fiveHours() {
		return LampRow.light(time.getHour() / 5, 4, Lamp.RED);
	}
}
