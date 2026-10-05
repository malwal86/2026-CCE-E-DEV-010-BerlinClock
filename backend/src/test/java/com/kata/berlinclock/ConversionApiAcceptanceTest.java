package com.kata.berlinclock;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;

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

	private void convert(String time) {
		client.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.body("{\"time\": \"%s\"}".formatted(time))
				.exchange()
				.expectStatus().isCreated();
	}
}
