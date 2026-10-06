package com.kata.berlinclock.clock;

import java.time.LocalTime;
import java.util.stream.IntStream;

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

	/** Four red lamps, one lit per hour left over after the five-hour blocks. */
	public LampRow singleHours() {
		return leftOverAfterFiveBlocks(time.getHour(), Lamp.RED);
	}

	/**
	 * Eleven lamps, one lit per full five minutes. Lit lamps at the quarter, half and three-quarter hour
	 * (positions 3, 6 and 9) are red; the others are yellow.
	 */
	public LampRow fiveMinutes() {
		int lit = time.getMinute() / 5;
		return new LampRow(IntStream.rangeClosed(1, 11)
				.mapToObj(position -> position > lit ? Lamp.OFF : fiveMinutesColour(position))
				.toList());
	}

	/** Four yellow lamps, one lit per minute left over after the five-minute blocks. */
	public LampRow singleMinutes() {
		return leftOverAfterFiveBlocks(time.getMinute(), Lamp.YELLOW);
	}

	/** Four lamps, one lit per unit left over after the blocks of five counted by the row above. */
	private static LampRow leftOverAfterFiveBlocks(int units, Lamp colour) {
		return LampRow.light(units % 5, 4, colour);
	}

	private static Lamp fiveMinutesColour(int position) {
		return position % 3 == 0 ? Lamp.RED : Lamp.YELLOW;
	}
}
