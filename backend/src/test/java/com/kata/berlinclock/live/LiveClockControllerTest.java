package com.kata.berlinclock.live;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;


@WebMvcTest(LiveClockController.class)
class LiveClockControllerTest {

	@Autowired
	private MockMvcTester mvc;

	@Test
	void showsTheBerlinClockForATime() {
		assertThat(mvc.get().uri("/api/berlin-clock").param("time", "16:50:06"))
				.hasStatusOk()
				.bodyJson().isStrictlyEqualTo("""
						{
						  "time": "16:50:06",
						  "clock": "YRRROROOOYYRYYRYYRYOOOOO",
						  "seconds": "Y",
						  "fiveHours": "RRRO",
						  "singleHours": "ROOO",
						  "fiveMinutes": "YYRYYRYYRYO",
						  "singleMinutes": "OOOO"
						}""");
	}

	@Test
	void anInvalidTimeIsABadRequestWithAProblemDetail() {
		assertThat(mvc.get().uri("/api/berlin-clock").param("time", "25:00:00"))
				.hasStatus(HttpStatus.BAD_REQUEST)
				.hasContentType(APPLICATION_PROBLEM_JSON)
				.bodyJson().isStrictlyEqualTo("""
						{
						  "title": "Invalid time",
						  "status": 400,
						  "detail": "Invalid time '25:00:00': expected HH:mm:ss between 00:00:00 and 23:59:59",
						  "instance": "/api/berlin-clock"
						}""");
	}

	@Test
	void aMissingTimeIsABadRequestWithAProblemDetail() {
		assertThat(mvc.get().uri("/api/berlin-clock"))
				.hasStatus(HttpStatus.BAD_REQUEST)
				.hasContentType(APPLICATION_PROBLEM_JSON)
				.bodyJson()
				.hasPathSatisfying("$.title", title -> assertThat(title).isEqualTo("Invalid time"))
				.hasPathSatisfying("$.detail", detail -> assertThat(detail).isEqualTo("A time is required (HH:mm:ss)"));
	}
}
