package com.kata.berlinclock.clock;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LampTest {

	@ParameterizedTest(name = "{0} is written {1}")
	@CsvSource({
			"YELLOW, Y",
			"RED, R",
			"OFF, O",
	})
	void isWrittenWithTheKataNotation(Lamp lamp, char symbol) {
		assertThat(lamp.symbol()).isEqualTo(symbol);
	}
}
