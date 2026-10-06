package com.kata.berlinclock.conversion.application;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kata.berlinclock.clock.DigitalTime;
import com.kata.berlinclock.clock.InvalidTimeException;

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

	/**
	 * @throws InvalidTimeException if {@code time} is missing or not a strict HH:mm:ss time; nothing is saved
	 */
	public Conversion convert(String time) {
		return history.save(DigitalTime.parse(time), clock.instant());
	}

	public List<Conversion> recent() {
		return history.latest(RECENT_LIMIT);
	}

	public Optional<Conversion> find(long id) {
		return history.findById(id);
	}
}
