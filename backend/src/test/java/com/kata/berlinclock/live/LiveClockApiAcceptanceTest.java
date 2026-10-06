package com.kata.berlinclock.live;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.kata.berlinclock.TestcontainersConfiguration;

/**
 * Story acceptance tests: the real application over real HTTP, with a real PostgreSQL that the live clock must not touch.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class LiveClockApiAcceptanceTest {

	@Autowired
	private RestTestClient client;

	@Autowired
	private JdbcClient jdbc;

	@BeforeEach
	void startWithAnEmptyHistory() {
		jdbc.sql("DELETE FROM conversion").update();
	}

	@Test
	void theLiveClockShowsATimeWithoutSavingIt() {
		client.get().uri("/api/berlin-clock?time=16:50:06")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.time").isEqualTo("16:50:06")
				.jsonPath("$.clock").isEqualTo("YRRROROOOYYRYYRYYRYOOOOO")
				.jsonPath("$.seconds").isEqualTo("Y")
				.jsonPath("$.fiveHours").isEqualTo("RRRO")
				.jsonPath("$.singleHours").isEqualTo("ROOO")
				.jsonPath("$.fiveMinutes").isEqualTo("YYRYYRYYRYO")
				.jsonPath("$.singleMinutes").isEqualTo("OOOO");

		assertThat(savedConversions()).isZero();
	}

	@Test
	void anInvalidTimeIsRejected() {
		client.get().uri("/api/berlin-clock?time=24:00:00")
				.exchange()
				.expectStatus().isBadRequest()
				.expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.expectBody()
				.jsonPath("$.title").isEqualTo("Invalid time")
				.jsonPath("$.detail").isEqualTo(
						"Invalid time '24:00:00': expected HH:mm:ss between 00:00:00 and 23:59:59")
				.jsonPath("$.instance").isEqualTo("/api/berlin-clock");
	}

	private long savedConversions() {
		return jdbc.sql("SELECT count(*) FROM conversion").query(Long.class).single();
	}
}
