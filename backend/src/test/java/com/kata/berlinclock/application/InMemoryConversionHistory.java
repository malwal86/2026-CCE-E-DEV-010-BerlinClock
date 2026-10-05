package com.kata.berlinclock.application;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test double for the history port: keeps conversions in memory, newest last.
 */
public class InMemoryConversionHistory implements ConversionHistory {

	private final List<Conversion> conversions = new ArrayList<>();

	@Override
	public Conversion save(LocalTime time, Instant convertedAt) {
		var conversion = new Conversion(conversions.size() + 1L, time, convertedAt);
		conversions.add(conversion);
		return conversion;
	}

	@Override
	public List<Conversion> latest(int limit) {
		return conversions.reversed().stream().limit(limit).toList();
	}

	public void clear() {
		conversions.clear();
	}
}
