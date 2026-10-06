package com.kata.berlinclock.conversion.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.kata.berlinclock.clock.InvalidTimeException;
import com.kata.berlinclock.clock.Lamp;

public class ConversionServiceTest {

	private static final Instant NOW = Instant.parse("2026-10-05T14:03:12Z");

	private final InMemoryConversionHistory history = new InMemoryConversionHistory();
	private final ConversionService service = new ConversionService(history, Clock.fixed(NOW, ZoneOffset.UTC));

	@Test
	void savesTheConvertedTimeWithTheMomentOfConversion() {
		var conversion = service.convert("16:50:06");

		assertThat(conversion.time()).isEqualTo(LocalTime.parse("16:50:06"));
		assertThat(conversion.convertedAt()).isEqualTo(NOW);
		assertThat(history.latest(10)).containsExactly(conversion);
	}

	@Test
	void listsTheTenMostRecentConversionsNewestFirst() {
		for (int second = 0; second <= 10; second++) {
			service.convert("00:00:%02d".formatted(second));
		}

		assertThat(service.recent())
				.hasSize(10)
				.extracting(Conversion::time)
				.startsWith(LocalTime.of(0, 0, 10))
				.endsWith(LocalTime.of(0, 0, 1));
	}

	@Test
	void aConversionShowsItsTimeOnTheBerlinClock() {
		var conversion = service.convert("23:59:59");

		assertThat(conversion.berlinClock().seconds()).isEqualTo(Lamp.OFF);
	}

	@Test
	void findsASavedConversionById() {
		service.convert("00:00:00");
		var saved = service.convert("23:59:59");

		assertThat(service.find(saved.id())).contains(saved);
	}

	@Test
	void findsNothingForAnUnknownId() {
		service.convert("23:59:59");

		assertThat(service.find(999_999)).isEmpty();
	}

	@Test
	void findingAConversionSavesNothing() {
		var saved = service.convert("23:59:59");

		service.find(saved.id());

		assertThat(history.latest(10)).containsExactly(saved);
	}

	@Test
	void clearingEmptiesTheHistory() {
		var saved = service.convert("00:00:00");
		service.convert("23:59:59");

		service.clear();

		assertThat(service.recent()).isEmpty();
		assertThat(service.find(saved.id())).isEmpty();
	}

	@Test
	void anInvalidTimeIsNeverSaved() {
		assertThatThrownBy(() -> service.convert("25:00:00")).isInstanceOf(InvalidTimeException.class);

		assertThat(history.latest(10)).isEmpty();
	}

	@Test
	void aMissingTimeIsNeverSaved() {
		assertThatThrownBy(() -> service.convert(null)).isInstanceOf(InvalidTimeException.class);

		assertThat(history.latest(10)).isEmpty();
	}
}
