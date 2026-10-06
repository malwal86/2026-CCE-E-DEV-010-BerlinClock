package com.kata.berlinclock.live;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kata.berlinclock.clock.DigitalTime;

/**
 * Read-only: the browser sends its own local time every second, so nothing here is saved
 * (a tick a second would bury the real history) and the server's time zone never matters.
 */
@RestController
class LiveClockController {

	/** A missing time reaches {@link DigitalTime#parse} too, so it gets the same problem detail as a POST. */
	@GetMapping("/api/berlin-clock")
	BerlinClockResponse show(@RequestParam(required = false) String time) {
		return BerlinClockResponse.of(DigitalTime.parse(time));
	}
}
