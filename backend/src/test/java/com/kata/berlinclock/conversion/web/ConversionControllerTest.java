package com.kata.berlinclock.conversion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

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
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.kata.berlinclock.conversion.application.ConversionService;
import com.kata.berlinclock.conversion.application.InMemoryConversionHistory;


@WebMvcTest(ConversionController.class)
@Import({ ConversionService.class, ConversionControllerTest.InMemoryApplication.class })
class ConversionControllerTest {

	private static final Instant NOW = Instant.parse("2026-10-05T14:03:12Z");

	@Autowired
	private MockMvcTester mvc;

	@Autowired
	private ConversionService service;

	@Autowired
	private InMemoryConversionHistory history;

	@BeforeEach
	void startWithAnEmptyHistory() {
		history.clear();
	}

	@Test
	void convertingATimeCreatesAConversion() {
		assertThat(mvc.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.content("""
						{"time": "00:00:00"}"""))
				.hasStatus(HttpStatus.CREATED)
				.hasHeader("Location", "http://localhost/api/conversions/1")
				.bodyJson().isStrictlyEqualTo("""
						{
						  "id": 1,
						  "time": "00:00:00",
						  "convertedAt": "2026-10-05T14:03:12Z",
						  "seconds": "Y",
						  "fiveHours": "OOOO",
						  "singleHours": "OOOO"
						}""");
	}

	@Test
	void listsRecentConversionsNewestFirst() {
		service.convert("00:00:00");
		service.convert("23:59:59");

		assertThat(mvc.get().uri("/api/conversions"))
				.hasStatusOk()
				.bodyJson().isStrictlyEqualTo("""
						[
						  { "id": 2, "time": "23:59:59", "convertedAt": "2026-10-05T14:03:12Z", "seconds": "O", "fiveHours": "RRRR", "singleHours": "RRRO" },
						  { "id": 1, "time": "00:00:00", "convertedAt": "2026-10-05T14:03:12Z", "seconds": "Y", "fiveHours": "OOOO", "singleHours": "OOOO" }
						]""");
	}

	@Test
	void anInvalidTimeIsABadRequestWithAProblemDetail() {
		assertThat(mvc.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.content("""
						{"time": "25:00:00"}"""))
				.hasStatus(HttpStatus.BAD_REQUEST)
				.hasContentType(APPLICATION_PROBLEM_JSON)
				.bodyJson().isStrictlyEqualTo("""
						{
						  "title": "Invalid time",
						  "status": 400,
						  "detail": "Invalid time '25:00:00': expected HH:mm:ss between 00:00:00 and 23:59:59",
						  "instance": "/api/conversions"
						}""");
		assertThat(history.latest(10)).isEmpty();
	}

	@Test
	void aMissingTimeIsABadRequestWithAProblemDetail() {
		assertThat(mvc.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.content("{}"))
				.hasStatus(HttpStatus.BAD_REQUEST)
				.hasContentType(APPLICATION_PROBLEM_JSON)
				.bodyJson()
				.hasPathSatisfying("$.title", title -> assertThat(title).isEqualTo("Invalid time"))
				.hasPathSatisfying("$.detail", detail -> assertThat(detail).isEqualTo("A time is required (HH:mm:ss)"));
	}

	@Test
	void aBodyThatIsNotJsonIsABadRequestWithAProblemDetail() {
		assertThat(mvc.post().uri("/api/conversions")
				.contentType(APPLICATION_JSON)
				.content("not json"))
				.hasStatus(HttpStatus.BAD_REQUEST)
				.hasContentType(APPLICATION_PROBLEM_JSON);
	}

	@TestConfiguration
	static class InMemoryApplication {

		@Bean
		InMemoryConversionHistory conversionHistory() {
			return new InMemoryConversionHistory();
		}

		@Bean
		Clock clock() {
			return Clock.fixed(NOW, ZoneOffset.UTC);
		}
	}
}
