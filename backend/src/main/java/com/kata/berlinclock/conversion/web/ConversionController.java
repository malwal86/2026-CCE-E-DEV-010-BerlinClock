package com.kata.berlinclock.conversion.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.kata.berlinclock.conversion.application.ConversionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/conversions")
@Tag(name = "Conversions", description = "Convert a time and keep it in the history")
class ConversionController {

	private static final String PROBLEM_JSON = "application/problem+json";

	private static final String HISTORY_UNAVAILABLE = """
			{"title": "History unavailable", "status": 503, "detail": "History is temporarily unavailable.",
			 "instance": "/api/conversions"}""";

	private final ConversionService service;

	@Autowired
	ConversionController(ConversionService service) {
		this.service = service;
	}

	@PostMapping
	@Operation(summary = "Convert a time and save it in the history")
	@ApiResponse(responseCode = "201", description = "Converted and saved",
			headers = @Header(name = "Location", description = "Where to read this conversion again",
					schema = @Schema(type = "string", format = "uri",
							example = "http://localhost:8080/api/conversions/12")))
	@ApiResponse(responseCode = "400", description = "The time is missing or not a valid HH:mm:ss; nothing is saved",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
					examples = @ExampleObject("""
							{"title": "Invalid time", "status": 400,
							 "detail": "Invalid time '25:00:00': expected HH:mm:ss between 00:00:00 and 23:59:59",
							 "instance": "/api/conversions"}""")))
	@ApiResponse(responseCode = "503", description = "The history's database is down; nothing is saved",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
					examples = @ExampleObject("""
							{"title": "History unavailable", "status": 503,
							 "detail": "History is temporarily unavailable. Your conversion was not saved.",
							 "instance": "/api/conversions"}""")))
	ResponseEntity<ConversionResponse> convert(@RequestBody ConversionRequest request, UriComponentsBuilder uri) {
		var conversion = service.convert(request.time());
		var location = uri.path("/api/conversions/{id}").buildAndExpand(conversion.id()).toUri();
		return ResponseEntity.created(location).body(ConversionResponse.from(conversion));
	}

	@GetMapping
	@Operation(summary = "The latest 10 conversions, newest first")
	@ApiResponse(responseCode = "200", description = "The latest conversions, possibly none")
	@ApiResponse(responseCode = "503", description = "The history's database is down",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
					examples = @ExampleObject(HISTORY_UNAVAILABLE)))
	List<ConversionResponse> recent() {
		return service.recent().stream().map(ConversionResponse::from).toList();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Read a past conversion again")
	@ApiResponse(responseCode = "200", description = "The conversion")
	@ApiResponse(responseCode = "404", description = "No conversion has this id",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
					examples = @ExampleObject("""
							{"title": "Conversion not found", "status": 404, "detail": "Conversion 999999 not found",
							 "instance": "/api/conversions/999999"}""")))
	@ApiResponse(responseCode = "503", description = "The history's database is down",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
					examples = @ExampleObject("""
							{"title": "History unavailable", "status": 503, "detail": "History is temporarily unavailable.",
							 "instance": "/api/conversions/12"}""")))
	ConversionResponse find(@Parameter(description = "Id of the conversion", example = "12") @PathVariable long id) {
		return service.find(id).map(ConversionResponse::from).orElseThrow(() -> new ConversionNotFoundException(id));
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Clear the history", description = "Deletes every conversion. Clearing an empty history is fine too.")
	@ApiResponse(responseCode = "204", description = "The history is empty")
	@ApiResponse(responseCode = "503", description = "The history's database is down; nothing is deleted",
			content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
					examples = @ExampleObject(HISTORY_UNAVAILABLE)))
	void clear() {
		service.clear();
	}
}
