package com.kata.berlinclock.conversion.application;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Test double for the history port: keeps conversions in memory, newest last. It can be made to fail, as the real
 * one does when the database is down.
 */
public class InMemoryConversionHistory implements ConversionHistory {

	private final List<Conversion> conversions = new ArrayList<>();

	private RuntimeException failure;

	/** From now on every call throws {@code failure}; {@link #recover()} undoes it. */
	public void failWith(RuntimeException failure) {
		this.failure = failure;
	}

	public void recover() {
		this.failure = null;
	}

	private void failIfBroken() {
		if (failure != null) {
			throw failure;
		}
	}

	@Override
	public Conversion save(LocalTime time, Instant convertedAt) {
		failIfBroken();
		var conversion = new Conversion(conversions.size() + 1L, time, convertedAt);
		conversions.add(conversion);
		return conversion;
	}

	@Override
	public List<Conversion> latest(int limit) {
		failIfBroken();
		return conversions.reversed().stream().limit(limit).toList();
	}

	@Override
	public Optional<Conversion> findById(long id) {
		failIfBroken();
		return conversions.stream().filter(conversion -> conversion.id() == id).findFirst();
	}

	@Override
	public void deleteAll() {
		failIfBroken();
		conversions.clear();
	}
}
