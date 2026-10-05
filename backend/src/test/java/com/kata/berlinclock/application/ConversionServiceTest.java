package com.kata.berlinclock.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.kata.berlinclock.domain.Lamp;

class ConversionServiceTest {

	private static final Instant NOW = Instant.parse("2026-10-05T14:03:12Z");

	private final InMemoryConversionHistory history = new InMemoryConversionHistory();
	private final ConversionService service = new ConversionService(history, Clock.fixed(NOW, ZoneOffset.UTC));

	@Test
	void savesTheConvertedTimeWithTheMomentOfConversion() {
		var conversion = service.convert(LocalTime.parse("16:50:06"));

		assertThat(conversion.time()).isEqualTo(LocalTime.parse("16:50:06"));
		assertThat(conversion.convertedAt()).isEqualTo(NOW);
		assertThat(history.latest(10)).containsExactly(conversion);
	}

	@Test
	void listsTheTenMostRecentConversionsNewestFirst() {
		for (int second = 0; second <= 10; second++) {
			service.convert(LocalTime.of(0, 0, second));
		}

		assertThat(service.recent())
				.hasSize(10)
				.extracting(Conversion::time)
				.startsWith(LocalTime.of(0, 0, 10))
				.endsWith(LocalTime.of(0, 0, 1));
	}

	@Test
	void aConversionShowsItsTimeOnTheBerlinClock() {
		var conversion = service.convert(LocalTime.parse("23:59:59"));

		assertThat(conversion.berlinClock().seconds()).isEqualTo(Lamp.OFF);
	}
}
