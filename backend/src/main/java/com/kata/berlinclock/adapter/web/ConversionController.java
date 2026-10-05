package com.kata.berlinclock.adapter.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.kata.berlinclock.application.ConversionService;

@RestController
@RequestMapping("/api/conversions")
class ConversionController {

	private final ConversionService service;

	ConversionController(ConversionService service) {
		this.service = service;
	}

	@PostMapping
	ResponseEntity<ConversionResponse> convert(@RequestBody ConversionRequest request, UriComponentsBuilder uri) {
		var conversion = service.convert(request.time());
		var location = uri.path("/api/conversions/{id}").buildAndExpand(conversion.id()).toUri();
		return ResponseEntity.created(location).body(ConversionResponse.from(conversion));
	}

	@GetMapping
	List<ConversionResponse> recent() {
		return service.recent().stream().map(ConversionResponse::from).toList();
	}
}
