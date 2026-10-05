package com.kata.berlinclock.clock;

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
