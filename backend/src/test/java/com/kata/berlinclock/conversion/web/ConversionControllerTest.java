package com.kata.berlinclock.conversion.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
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
						  "seconds": "Y"
						}""");
	}

	@Test
	void listsRecentConversionsNewestFirst() {
		service.convert(LocalTime.parse("00:00:00"));
		service.convert(LocalTime.parse("23:59:59"));

		assertThat(mvc.get().uri("/api/conversions"))
				.hasStatusOk()
				.bodyJson().isStrictlyEqualTo("""
						[
						  { "id": 2, "time": "23:59:59", "convertedAt": "2026-10-05T14:03:12Z", "seconds": "O" },
						  { "id": 1, "time": "00:00:00", "convertedAt": "2026-10-05T14:03:12Z", "seconds": "Y" }
						]""");
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
