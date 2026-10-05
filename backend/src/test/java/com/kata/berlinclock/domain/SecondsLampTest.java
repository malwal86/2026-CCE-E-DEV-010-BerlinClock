package com.kata.berlinclock.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SecondsLampTest {

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
			"00:00:00, YELLOW",
			"23:59:59, OFF",
			"12:00:02, YELLOW",
			"12:00:01, OFF",
	})
	void blinksEverySecond(LocalTime time, Lamp expected) {
		assertThat(BerlinClock.of(time).seconds()).isEqualTo(expected);
	}
}
