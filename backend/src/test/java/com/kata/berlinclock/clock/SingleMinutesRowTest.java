package com.kata.berlinclock.clock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SingleMinutesRowTest {

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
			"00:00:00, OOOO",
			"23:59:59, YYYY",
			"12:32:00, YYOO",
			"12:34:00, YYYY",
			"12:35:00, OOOO",
	})
	void lightsOneYellowLampPerMinuteLeftOverAfterTheFiveMinuteBlocks(LocalTime time, String expected) {
		assertThat(BerlinClock.of(time).singleMinutes().notation()).isEqualTo(expected);
	}
}
