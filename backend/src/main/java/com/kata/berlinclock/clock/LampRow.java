package com.kata.berlinclock.clock;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A row of lamps, read from left to right.
 */
public record LampRow(List<Lamp> lamps) {

	public LampRow {
		lamps = List.copyOf(lamps);
	}

	/** A row of {@code size} lamps whose first {@code lit} shine in {@code colour}; the rest are off. */
	static LampRow light(int lit, int size, Lamp colour) {
		return new LampRow(Stream.concat(
				Collections.nCopies(lit, colour).stream(),
				Collections.nCopies(size - lit, Lamp.OFF).stream())
				.toList());
	}

	/** The row in the kata notation, e.g. {@code RRRO}. */
	public String notation() {
		return lamps.stream().map(lamp -> String.valueOf(lamp.symbol())).collect(Collectors.joining());
	}
}
