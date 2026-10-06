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

import io.swagger.v3.parser.OpenAPIV3Parser;

/**
 * Story D1 acceptance tests: the running application serves its API contract, as an OpenAPI 3 document and as a
 * Swagger UI page where every request can be tried. {@link ApiContractTest} checks the contract matches the code.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class ApiDocumentationAcceptanceTest {

	@Autowired
	private RestTestClient client;

	@Test
	void theOpenApiDocumentIsValidAndListsEveryEndpoint() {
		var document = client.get().uri("/v3/api-docs")
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class).returnResult().getResponseBody();

		var parsed = new OpenAPIV3Parser().readContents(document);

		assertThat(parsed.getMessages()).isEmpty();
		var paths = parsed.getOpenAPI().getPaths();
		assertThat(paths.get("/api/conversions").getPost()).isNotNull();
		assertThat(paths.get("/api/conversions").getGet()).isNotNull();
		assertThat(paths.get("/api/conversions").getDelete()).isNotNull();
		assertThat(paths.get("/api/conversions/{id}").getGet()).isNotNull();
		assertThat(paths.get("/api/berlin-clock").getGet()).isNotNull();
	}

	@Test
	void theSwaggerUiIsServedWithTheContract() {
		client.get().uri("/swagger-ui.html")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
				.expectBody(String.class).value(page -> assertThat(page).contains("url: '/openapi.yaml'"));
		client.get().uri("/webjars/swagger-ui/swagger-ui-bundle.js")
				.exchange()
				.expectStatus().isOk();
		client.get().uri("/openapi.yaml")
				.exchange()
				.expectStatus().isOk();
	}
}
