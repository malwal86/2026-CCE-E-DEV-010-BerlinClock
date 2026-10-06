package com.kata.berlinclock;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * The API contract is the hand-written {@code static/openapi.yaml}, shown by {@code static/swagger-ui.html}. It is also
 * served at {@code /v3/api-docs}, where OpenAPI tools usually look for it.
 */
@Configuration
class ApiDocumentationConfiguration implements WebMvcConfigurer {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/v3/api-docs").setViewName("forward:/openapi.yaml");
	}
}
