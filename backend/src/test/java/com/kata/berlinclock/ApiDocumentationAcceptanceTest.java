package com.kata.berlinclock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Story D1 acceptance tests: the running application describes its own API, as an OpenAPI 3 document and as a
 * Swagger UI page where every request can be tried.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class ApiDocumentationAcceptanceTest {

	private static final String PROBLEM = "#/components/schemas/ProblemDetail";

	@Autowired
	private RestTestClient client;

	@Test
	void theOpenApiDocumentListsEveryEndpoint() {
		client.get().uri("/v3/api-docs")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.openapi").value(String.class, version -> assertThat(version).startsWith("3."))
				.jsonPath("$.paths['/api/conversions'].post").exists()
				.jsonPath("$.paths['/api/conversions'].get").exists()
				.jsonPath("$.paths['/api/conversions'].delete").exists()
				.jsonPath("$.paths['/api/conversions/{id}'].get").exists()
				.jsonPath("$.paths['/api/berlin-clock'].get").exists();
	}

	@Test
	void theTimeIsDocumentedWithItsPatternAndAKataExample() {
		client.get().uri("/v3/api-docs")
				.exchange()
				.expectBody()
				.jsonPath("$.components.schemas.ConversionRequest.properties.time.pattern")
				.isEqualTo("^\\d{2}:\\d{2}:\\d{2}$")
				.jsonPath("$.components.schemas.ConversionRequest.properties.time.example").isEqualTo("16:50:06")
				.jsonPath("$.paths['/api/berlin-clock'].get.parameters[0].schema.pattern")
				.isEqualTo("^\\d{2}:\\d{2}:\\d{2}$")
				.jsonPath("$.paths['/api/berlin-clock'].get.parameters[0].example").isEqualTo("16:50:06")
				.jsonPath("$.components.schemas.ConversionResponse.properties.clock.example")
				.isEqualTo("YRRROROOOYYRYYRYYRYOOOOO");
	}

	@Test
	void everyErrorIsDocumentedAsAProblemDetail() {
		client.get().uri("/v3/api-docs")
				.exchange()
				.expectBody()
				.jsonPath("$.paths['/api/conversions'].post.responses['201']").exists()
				.jsonPath("$.paths['/api/conversions'].post.responses['400'].content['application/problem+json'].schema.$ref")
				.isEqualTo(PROBLEM)
				.jsonPath("$.paths['/api/conversions'].post.responses['503'].content['application/problem+json'].schema.$ref")
				.isEqualTo(PROBLEM)
				.jsonPath("$.paths['/api/conversions'].get.responses['503'].content['application/problem+json'].schema.$ref")
				.isEqualTo(PROBLEM)
				.jsonPath("$.paths['/api/conversions'].delete.responses['204']").exists()
				.jsonPath("$.paths['/api/conversions'].delete.responses['503'].content['application/problem+json'].schema.$ref")
				.isEqualTo(PROBLEM)
				.jsonPath("$.paths['/api/conversions/{id}'].get.responses['404'].content['application/problem+json'].schema.$ref")
				.isEqualTo(PROBLEM)
				.jsonPath("$.paths['/api/conversions/{id}'].get.responses['503'].content['application/problem+json'].schema.$ref")
				.isEqualTo(PROBLEM)
				.jsonPath("$.paths['/api/berlin-clock'].get.responses['400'].content['application/problem+json'].schema.$ref")
				.isEqualTo(PROBLEM)
				.jsonPath("$.paths['/api/berlin-clock'].get.responses['503']").doesNotExist();
	}

	@Test
	void theSwaggerUiIsServed() {
		client.get().uri("/swagger-ui.html")
				.exchange()
				.expectStatus().is3xxRedirection();
		client.get().uri("/swagger-ui/index.html")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
	}
}
