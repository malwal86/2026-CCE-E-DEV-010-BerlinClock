package com.kata.berlinclock.conversion.application;

import java.time.Clock;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConversionService {

	static final int RECENT_LIMIT = 10;

	private final ConversionHistory history;
	private final Clock clock;

	@Autowired
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
