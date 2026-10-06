package com.kata.berlinclock.clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class DigitalTimeTest {

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "00:00:00", "23:59:59", "16:50:06" })
	void parsesATimeOfDay(String text) {
		assertThat(DigitalTime.parse(text)).isEqualTo(LocalTime.parse(text));
	}

	@ParameterizedTest(name = "{0}: {1}")
	@CsvSource(delimiter = '|', value = {
			"25:00:00 | hour out of range",
			"24:00:00 | 24:00 not supported",
			"12:60:00 | minute out of range",
			"12:00:60 | second out of range",
			"1:2:3    | not zero-padded",
			"12-00-00 | wrong separator",
			"12:00    | seconds missing",
			"noon     | not a time",
	})
	void rejectsAnInvalidTime(String text, String reason) {
		assertThatThrownBy(() -> DigitalTime.parse(text))
				.isInstanceOf(InvalidTimeException.class)
				.hasMessage("Invalid time '%s': expected HH:mm:ss between 00:00:00 and 23:59:59", text);
	}

	@ParameterizedTest(name = "\"{0}\"")
	@NullSource
	@ValueSource(strings = { "", "   " })
	void requiresATime(String text) {
		assertThatThrownBy(() -> DigitalTime.parse(text))
				.isInstanceOf(InvalidTimeException.class)
				.hasMessage("A time is required (HH:mm:ss)");
	}
}
