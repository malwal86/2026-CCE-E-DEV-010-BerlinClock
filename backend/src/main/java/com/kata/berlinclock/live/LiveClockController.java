package com.kata.berlinclock.live;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kata.berlinclock.clock.DigitalTime;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Read-only: the browser sends its own local time every second, so nothing here is saved
 * (a tick a second would bury the real history) and the server's time zone never matters.
 */
@RestController
@Tag(name = "Live clock", description = "Show any time as a Berlin Clock without saving it")
class LiveClockController {

	/** A missing time reaches {@link DigitalTime#parse} too, so it gets the same problem detail as a POST. */
	@GetMapping("/api/berlin-clock")
	@Operation(summary = "Show a time as a Berlin Clock, without saving it",
			description = "Used by the live clock every second. The database is never touched.")
	@ApiResponse(responseCode = "200", description = "The time as a Berlin Clock")
	@ApiResponse(responseCode = "400", description = "The time is missing or not a valid HH:mm:ss",
			content = @Content(mediaType = "application/problem+json",
					schema = @Schema(implementation = ProblemDetail.class),
					examples = @ExampleObject("""
							{"title": "Invalid time", "status": 400,
							 "detail": "Invalid time '24:00:00': expected HH:mm:ss between 00:00:00 and 23:59:59",
							 "instance": "/api/berlin-clock"}""")))
	BerlinClockResponse show(
			@Parameter(description = "Time to show, zero-padded, from 00:00:00 to 23:59:59", required = true,
					example = "16:50:06", schema = @Schema(type = "string", pattern = "^\\d{2}:\\d{2}:\\d{2}$"))
			@RequestParam(required = false) String time) {
		return BerlinClockResponse.of(DigitalTime.parse(time));
	}
}
