package com.kata.berlinclock;

import static com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers.openApi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MockMvcTester.MockMvcRequestBuilder;

import com.kata.berlinclock.conversion.application.ConversionService;
import com.kata.berlinclock.conversion.application.InMemoryConversionHistory;

/**
 * The hand-written contract ({@code static/openapi.yaml}) against the real controllers: every documented answer of
 * every endpoint is produced here, and both the request and the response must match the contract.
 */
@WebMvcTest
@Import({ ConversionService.class, ApiContractTest.InMemoryApplication.class })
class ApiContractTest {

	private static final ResultMatcher MATCHES_THE_CONTRACT = openApi().isValid("static/openapi.yaml");

	@Autowired
	private MockMvcTester mvc;

	@Autowired
	private InMemoryConversionHistory history;

	@BeforeEach
	void startWithAWorkingEmptyHistory() {
		history.recover();
		history.deleteAll();
	}

	@Test
	void convertingATime() {
		assertMatchesTheContract(convert("16:50:06"), HttpStatus.CREATED);
	}

	@Test
	void convertingAnInvalidTime() {
		assertMatchesTheContract(convert("25:00:00"), HttpStatus.BAD_REQUEST);
	}

	@Test
	void convertingWhileTheDatabaseIsDown() {
		givenTheDatabaseIsDown();

		assertMatchesTheContract(convert("16:50:06"), HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	void readingTheHistory() {
		givenConversions("16:50:06", "00:00:00");

		assertMatchesTheContract(mvc.get().uri("/api/conversions"), HttpStatus.OK);
	}

	@Test
	void readingTheHistoryWhileTheDatabaseIsDown() {
		givenTheDatabaseIsDown();

		assertMatchesTheContract(mvc.get().uri("/api/conversions"), HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	void readingAConversionAgain() {
		givenConversions("16:50:06");

		assertMatchesTheContract(mvc.get().uri("/api/conversions/1"), HttpStatus.OK);
	}

	@Test
	void readingAnUnknownConversion() {
		assertMatchesTheContract(mvc.get().uri("/api/conversions/999999"), HttpStatus.NOT_FOUND);
	}

	@Test
	void readingAConversionWhileTheDatabaseIsDown() {
		givenTheDatabaseIsDown();

		assertMatchesTheContract(mvc.get().uri("/api/conversions/1"), HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	void clearingTheHistory() {
		givenConversions("16:50:06");

		assertMatchesTheContract(mvc.delete().uri("/api/conversions"), HttpStatus.NO_CONTENT);
	}

	@Test
	void clearingTheHistoryWhileTheDatabaseIsDown() {
		givenTheDatabaseIsDown();

		assertMatchesTheContract(mvc.delete().uri("/api/conversions"), HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	void showingATimeOnTheLiveClock() {
		assertMatchesTheContract(mvc.get().uri("/api/berlin-clock?time=16:50:06"), HttpStatus.OK);
	}

	@Test
	void showingAnInvalidTimeOnTheLiveClock() {
		assertMatchesTheContract(mvc.get().uri("/api/berlin-clock?time=24:00:00"), HttpStatus.BAD_REQUEST);
	}

	private MockMvcRequestBuilder convert(String time) {
		return mvc.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.content("""
						{"time": "%s"}""".formatted(time));
	}

	/** The status is checked too, so each test really produces the answer it is named after. */
	private void assertMatchesTheContract(MockMvcRequestBuilder request, HttpStatus status) {
		assertThat(request).hasStatus(status).matches(MATCHES_THE_CONTRACT);
	}

	private void givenConversions(String... times) {
		for (var time : times) {
			assertThat(convert(time)).hasStatus(HttpStatus.CREATED);
		}
	}

	private void givenTheDatabaseIsDown() {
		history.failWith(new DataAccessResourceFailureException("Connection refused"));
	}

	@TestConfiguration
	static class InMemoryApplication {

		@Bean
		InMemoryConversionHistory conversionHistory() {
			return new InMemoryConversionHistory();
		}

		@Bean
		Clock clock() {
			return Clock.fixed(Instant.parse("2026-10-05T14:03:12Z"), ZoneOffset.UTC);
		}
	}
}
