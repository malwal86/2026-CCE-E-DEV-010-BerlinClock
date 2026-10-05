package com.kata.berlinclock.domain;

public enum Lamp {
	YELLOW('Y'),
	OFF('O');

	private final char symbol;

	Lamp(char symbol) {
		this.symbol = symbol;
	}

	public char symbol() {
		return symbol;
	}
}
