package com.kata.berlinclock;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kata.berlinclock.application.ConversionHistory;
import com.kata.berlinclock.application.ConversionService;

/**
 * Wires the framework-free application layer into Spring.
 */
@Configuration(proxyBeanMethods = false)
class ApplicationConfiguration {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	ConversionService conversionService(ConversionHistory history, Clock clock) {
		return new ConversionService(history, clock);
	}
}
