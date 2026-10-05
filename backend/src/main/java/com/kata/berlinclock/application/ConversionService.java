package com.kata.berlinclock.application;

import java.time.Clock;
import java.time.LocalTime;
import java.util.List;

public class ConversionService {

	static final int RECENT_LIMIT = 10;

	private final ConversionHistory history;
	private final Clock clock;

	public ConversionService(ConversionHistory history, Clock clock) {
		this.history = history;
		this.clock = clock;
	}

	public Conversion convert(LocalTime time) {
		return history.save(time, clock.instant());
	}

	public List<Conversion> recent() {
		return history.latest(RECENT_LIMIT);
	}
}
