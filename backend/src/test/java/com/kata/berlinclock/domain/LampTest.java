package com.kata.berlinclock.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LampTest {

	@ParameterizedTest(name = "{0} is written {1}")
	@CsvSource({
			"YELLOW, Y",
			"OFF, O",
	})
	void isWrittenWithTheKataNotation(Lamp lamp, char symbol) {
		assertThat(lamp.symbol()).isEqualTo(symbol);
	}
}
