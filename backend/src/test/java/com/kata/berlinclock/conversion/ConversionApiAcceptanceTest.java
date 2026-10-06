package com.kata.berlinclock.conversion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.kata.berlinclock.TestcontainersConfiguration;

/**
 * Story acceptance tests: the real application over real HTTP, down to a real PostgreSQL.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class ConversionApiAcceptanceTest {

	@Autowired
	private RestTestClient client;

	@Autowired
	private JdbcClient jdbc;

	@BeforeEach
	void startWithAnEmptyHistory() {
		jdbc.sql("DELETE FROM conversion").update();
	}

	@Test
	void convertingATimeShowsItsSecondsLampAndSavesTheConversion() {
		client.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.body("""
						{"time": "00:00:00"}""")
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().valueMatches("Location", ".*/api/conversions/\\d+$")
				.expectBody()
				.jsonPath("$.id").isNumber()
				.jsonPath("$.time").isEqualTo("00:00:00")
				.jsonPath("$.convertedAt").isNotEmpty()
				.jsonPath("$.seconds").isEqualTo("Y");
	}

	@Test
	void recentConversionsAreListedNewestFirst() {
		convert("00:00:00");
		convert("23:59:59");

		client.get().uri("/api/conversions")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.length()").isEqualTo(2)
				.jsonPath("$[0].time").isEqualTo("23:59:59")
				.jsonPath("$[0].seconds").isEqualTo("O")
				.jsonPath("$[1].time").isEqualTo("00:00:00")
				.jsonPath("$[1].seconds").isEqualTo("Y");
	}

	@Test
	void onlyTheTenMostRecentConversionsAreListed() {
		for (int second = 0; second <= 10; second++) {
			convert("00:00:%02d".formatted(second));
		}

		client.get().uri("/api/conversions")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.length()").isEqualTo(10)
				.jsonPath("$[0].time").isEqualTo("00:00:10")
				.jsonPath("$[9].time").isEqualTo("00:00:01");
	}

	@Test
	void convertingATimeShowsItsFiveHoursRow() {
		client.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.body("""
						{"time": "16:35:00"}""")
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.fiveHours").isEqualTo("RRRO");

		client.get().uri("/api/conversions")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].time").isEqualTo("16:35:00")
				.jsonPath("$[0].fiveHours").isEqualTo("RRRO");
	}

	@Test
	void earlierConversionsGainTheFiveHoursRow() {
		jdbc.sql("INSERT INTO conversion (time, converted_at) VALUES ('23:59:59', '2026-10-05T14:03:12Z')").update();

		client.get().uri("/api/conversions")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].time").isEqualTo("23:59:59")
				.jsonPath("$[0].fiveHours").isEqualTo("RRRR");
	}

	@Test
	void convertingATimeShowsItsSingleHoursRow() {
		client.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.body("""
						{"time": "14:35:00"}""")
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.fiveHours").isEqualTo("RROO")
				.jsonPath("$.singleHours").isEqualTo("RRRR");

		client.get().uri("/api/conversions")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].time").isEqualTo("14:35:00")
				.jsonPath("$[0].singleHours").isEqualTo("RRRR");
	}

	@Test
	void earlierConversionsGainTheSingleHoursRow() {
		jdbc.sql("INSERT INTO conversion (time, converted_at) VALUES ('23:59:59', '2026-10-05T14:03:12Z')").update();

		client.get().uri("/api/conversions")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].time").isEqualTo("23:59:59")
				.jsonPath("$[0].singleHours").isEqualTo("RRRO");
	}

	@Test
	void convertingATimeShowsItsFiveMinutesRow() {
		client.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.body("""
						{"time": "12:35:00"}""")
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.fiveHours").isEqualTo("RROO")
				.jsonPath("$.singleHours").isEqualTo("RROO")
				.jsonPath("$.fiveMinutes").isEqualTo("YYRYYRYOOOO");

		client.get().uri("/api/conversions")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].time").isEqualTo("12:35:00")
				.jsonPath("$[0].fiveMinutes").isEqualTo("YYRYYRYOOOO");
	}

	@Test
	void earlierConversionsGainTheFiveMinutesRow() {
		jdbc.sql("INSERT INTO conversion (time, converted_at) VALUES ('23:59:59', '2026-10-05T14:03:12Z')").update();

		client.get().uri("/api/conversions")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].time").isEqualTo("23:59:59")
				.jsonPath("$[0].fiveMinutes").isEqualTo("YYRYYRYYRYY");
	}

	@Test
	void convertingATimeShowsItsSingleMinutesRow() {
		client.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.body("""
						{"time": "12:34:00"}""")
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.jsonPath("$.fiveHours").isEqualTo("RROO")
				.jsonPath("$.singleHours").isEqualTo("RROO")
				.jsonPath("$.fiveMinutes").isEqualTo("YYRYYROOOOO")
				.jsonPath("$.singleMinutes").isEqualTo("YYYY");

		client.get().uri("/api/conversions")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].time").isEqualTo("12:34:00")
				.jsonPath("$[0].singleMinutes").isEqualTo("YYYY");
	}

	@Test
	void earlierConversionsGainTheSingleMinutesRow() {
		jdbc.sql("INSERT INTO conversion (time, converted_at) VALUES ('12:32:00', '2026-10-05T14:03:12Z')").update();

		client.get().uri("/api/conversions")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$[0].time").isEqualTo("12:32:00")
				.jsonPath("$[0].singleMinutes").isEqualTo("YYOO");
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "25:00:00", "24:00:00", "12:60:00", "12:00:60", "1:2:3", "12-00-00", "12:00", "noon" })
	void anInvalidTimeIsRejectedAndNotSaved(String time) {
		client.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.body("{\"time\": \"%s\"}".formatted(time))
				.exchange()
				.expectStatus().isBadRequest()
				.expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.expectBody()
				.jsonPath("$.title").isEqualTo("Invalid time")
				.jsonPath("$.status").isEqualTo(400)
				.jsonPath("$.detail").isEqualTo(
						"Invalid time '%s': expected HH:mm:ss between 00:00:00 and 23:59:59".formatted(time))
				.jsonPath("$.instance").isEqualTo("/api/conversions");

		assertThat(savedConversions()).isZero();
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { "{\"time\": \"\"}", "{}" })
	void aMissingTimeIsRejectedAndNotSaved(String body) {
		client.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.body(body)
				.exchange()
				.expectStatus().isBadRequest()
				.expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.expectBody()
				.jsonPath("$.title").isEqualTo("Invalid time")
				.jsonPath("$.detail").isEqualTo("A time is required (HH:mm:ss)");

		assertThat(savedConversions()).isZero();
	}

	private long savedConversions() {
		return jdbc.sql("SELECT count(*) FROM conversion").query(Long.class).single();
	}

	private void convert(String time) {
		client.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.body("{\"time\": \"%s\"}".formatted(time))
				.exchange()
				.expectStatus().isCreated();
	}
}
