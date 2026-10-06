package com.kata.berlinclock.clock;

import static com.kata.berlinclock.clock.Lamp.OFF;
import static com.kata.berlinclock.clock.Lamp.RED;
import static com.kata.berlinclock.clock.Lamp.YELLOW;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LampRowTest {

	@ParameterizedTest(name = "{0} of 4 -> {1}")
	@CsvSource({
			"0, OOOO",
			"1, ROOO",
			"3, RRRO",
			"4, RRRR",
	})
	void lightsLampsFromTheLeft(int lit, String expected) {
		assertThat(LampRow.light(lit, 4, RED).notation()).isEqualTo(expected);
	}

	@Test
	void keepsItsLampsInOrder() {
		assertThat(LampRow.light(2, 3, YELLOW).lamps()).containsExactly(YELLOW, YELLOW, OFF);
	}
}
