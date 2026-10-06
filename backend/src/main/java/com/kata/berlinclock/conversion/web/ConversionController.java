package com.kata.berlinclock.conversion.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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


@RestController
@RequestMapping("/api/conversions")
class ConversionController {

	private final ConversionService service;

	@Autowired
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

	@GetMapping("/{id}")
	ConversionResponse find(@PathVariable long id) {
		return service.find(id).map(ConversionResponse::from).orElseThrow(() -> new ConversionNotFoundException(id));
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void clear() {
		service.clear();
	}
}
