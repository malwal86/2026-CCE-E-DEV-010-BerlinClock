package com.kata.berlinclock.conversion.application;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

/**
 * Port to wherever conversions are kept.
 */
public interface ConversionHistory {

	Conversion save(LocalTime time, Instant convertedAt);

	/** The {@code limit} most recent conversions, newest first. */
	List<Conversion> latest(int limit);
}
