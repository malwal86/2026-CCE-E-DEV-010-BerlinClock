package com.kata.berlinclock.clock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FiveMinutesRowTest {

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
			"00:00:00, OOOOOOOOOOO",
			"23:59:59, YYRYYRYYRYY",
			"12:04:00, OOOOOOOOOOO",
			"12:23:00, YYRYOOOOOOO",
			"12:35:00, YYRYYRYOOOO",
			"12:15:00, YYROOOOOOOO",
	})
	void lightsOneLampPerFullFiveMinutesWithRedQuarterMarkers(LocalTime time, String expected) {
		assertThat(BerlinClock.of(time).fiveMinutes().notation()).isEqualTo(expected);
	}
}
