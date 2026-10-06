package com.kata.berlinclock.clock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FiveHoursRowTest {

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
			"00:00:00, OOOO",
			"23:59:59, RRRR",
			"02:04:00, OOOO",
			"08:23:00, ROOO",
			"16:35:00, RRRO",
	})
	void lightsOneRedLampPerFullFiveHours(LocalTime time, String expected) {
		assertThat(BerlinClock.of(time).fiveHours().notation()).isEqualTo(expected);
	}
}
