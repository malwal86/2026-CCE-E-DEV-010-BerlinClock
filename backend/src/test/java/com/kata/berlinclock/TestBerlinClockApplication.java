package com.kata.berlinclock;

import org.springframework.boot.SpringApplication;

public class TestBerlinClockApplication {

	public static void main(String[] args) {
		SpringApplication.from(BerlinClockApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
