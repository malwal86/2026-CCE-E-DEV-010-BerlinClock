package com.kata.berlinclock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Berlin Clock API", version = "v1",
		description = "Convert a time (HH:mm:ss) to the Berlin Clock and keep a history of conversions. "
				+ "Errors are RFC 9457 problem details (application/problem+json)."))
public class BerlinClockApplication {

	public static void main(String[] args) {
		SpringApplication.run(BerlinClockApplication.class, args);
	}

}
