package com.kata.berlinclock.conversion.web;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The time stays raw text so the application layer applies the strict HH:mm:ss contract, not JSON binding.
 */
record ConversionRequest(
		@Schema(description = "Time to convert, zero-padded, from 00:00:00 to 23:59:59",
				pattern = "^\\d{2}:\\d{2}:\\d{2}$", example = "16:50:06",
				requiredMode = Schema.RequiredMode.REQUIRED)
		String time) {
}
