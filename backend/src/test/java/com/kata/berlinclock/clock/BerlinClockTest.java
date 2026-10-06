package com.kata.berlinclock.clock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BerlinClockTest {

	@ParameterizedTest(name = "{0} -> {1}")
	@CsvSource({
			"00:00:00, YOOOOOOOOOOOOOOOOOOOOOOO",
			"23:59:59, ORRRRRRROYYRYYRYYRYYYYYY",
			"16:50:06, YRRROROOOYYRYYRYYRYOOOOO",
			"11:37:01, ORROOROOOYYRYYRYOOOOYYOO",
	})
	void theEntireClockIsEveryRowTopToBottomInTheKataNotation(LocalTime time, String expected) {
		assertThat(BerlinClock.of(time).code()).isEqualTo(expected);
	}

	@Test
	void theCodeIsTwentyFourLampsLongForEverySecondOfTheDay() {
		IntStream.range(0, 24 * 60 * 60)
				.mapToObj(LocalTime.MIDNIGHT::plusSeconds)
				.forEach(time -> assertThat(BerlinClock.of(time).code()).as("%s", time).hasSize(24));
	}
}
