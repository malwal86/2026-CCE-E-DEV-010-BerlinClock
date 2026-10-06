package com.kata.berlinclock.clock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SingleHoursRowTest {

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
			"00:00:00, OOOO",
			"23:59:59, RRRO",
			"02:04:00, RROO",
			"08:23:00, RRRO",
			"14:35:00, RRRR",
	})
	void lightsOneRedLampPerHourLeftOverAfterTheFiveHourBlocks(LocalTime time, String expected) {
		assertThat(BerlinClock.of(time).singleHours().notation()).isEqualTo(expected);
	}
}
