package com.kata.berlinclock.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

import com.kata.berlinclock.TestcontainersConfiguration;
import com.kata.berlinclock.application.Conversion;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ TestcontainersConfiguration.class, JdbcConversionHistory.class })
class JdbcConversionHistoryTest {

	private static final Instant TEN_O_CLOCK = Instant.parse("2026-10-05T10:00:00Z");

	@Autowired
	private JdbcConversionHistory history;

	@Test
	void savesAConversionWithAGeneratedId() {
		var saved = history.save(LocalTime.parse("16:50:06"), TEN_O_CLOCK);

		assertThat(saved.id()).isPositive();
		assertThat(saved.time()).isEqualTo(LocalTime.parse("16:50:06"));
		assertThat(saved.convertedAt()).isEqualTo(TEN_O_CLOCK);
	}

	@Test
	void returnsExactlyWhatWasStored() {
		var saved = history.save(LocalTime.parse("23:59:59"), Instant.parse("2026-10-05T10:00:00.123456789Z"));

		assertThat(history.latest(1)).containsExactly(saved);
	}

	@Test
	void listsTheLatestConversionsNewestFirst() {
		var first = history.save(LocalTime.parse("01:00:00"), TEN_O_CLOCK);
		var second = history.save(LocalTime.parse("02:00:00"), TEN_O_CLOCK.plusSeconds(60));
		var third = history.save(LocalTime.parse("03:00:00"), TEN_O_CLOCK.plusSeconds(120));

		assertThat(history.latest(2)).containsExactly(third, second).doesNotContain(first);
	}

	@Test
	void conversionsMadeAtTheSameMomentAreListedLastSavedFirst() {
		var earlier = history.save(LocalTime.parse("01:00:00"), TEN_O_CLOCK);
		var later = history.save(LocalTime.parse("02:00:00"), TEN_O_CLOCK);

		assertThat(history.latest(10)).extracting(Conversion::id).containsExactly(later.id(), earlier.id());
	}
}
