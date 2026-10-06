package com.kata.berlinclock;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Story C2 acceptance test: the real application over real HTTP, after its PostgreSQL has been stopped. The database
 * is this test's own, so stopping it leaves the one the other tests share alone.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(DatabaseDownAcceptanceTest.OwnDatabase.class)
class DatabaseDownAcceptanceTest {

	@Autowired
	private RestTestClient client;

	@Autowired
	private PostgreSQLContainer database;

	@Test
	void withTheDatabaseStoppedConversionsAreRefusedButTheLiveClockKeepsTicking() {
		convert("12:00:00").expectStatus().isCreated();

		database.stop();

		convert("12:00:00")
				.expectStatus().isEqualTo(503)
				.expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.expectBody()
				.jsonPath("$.title").isEqualTo("History unavailable")
				.jsonPath("$.detail").isEqualTo("History is temporarily unavailable. Your conversion was not saved.");

		client.get().uri("/api/conversions")
				.exchange()
				.expectStatus().isEqualTo(503)
				.expectBody()
				.jsonPath("$.detail").isEqualTo("History is temporarily unavailable.");

		client.get().uri("/api/berlin-clock?time=12:00:00")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.clock").isEqualTo("YRROORROOOOOOOOOOOOOOOOO");
	}

	private RestTestClient.ResponseSpec convert(String time) {
		return client.post().uri("/api/conversions")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("time", time))
				.exchange();
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class OwnDatabase {

		@Bean
		@ServiceConnection
		PostgreSQLContainer ownPostgresContainer() {
			return new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));
		}
	}
}
