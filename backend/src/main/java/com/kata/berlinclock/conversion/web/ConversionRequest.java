package com.kata.berlinclock.conversion.web;

/**
 * The time stays raw text so the application layer applies the strict HH:mm:ss contract, not JSON binding.
 */
record ConversionRequest(String time) {
}
