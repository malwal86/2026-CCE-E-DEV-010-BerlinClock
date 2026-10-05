package com.kata.berlinclock;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Clock} is a JDK type that cannot carry a stereotype annotation, so it is
 * the one bean declared here; every other bean is a component in its own right.
 */
@Configuration(proxyBeanMethods = false)
class BerlinClockApplicationConfiguration {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
