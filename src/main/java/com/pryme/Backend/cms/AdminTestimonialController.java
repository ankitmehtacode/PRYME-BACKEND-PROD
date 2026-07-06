package com.pryme.Backend.cms;

import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
public class AdminTestimonialController {

    private final TestimonialService testimonialService;

    @Operation(summary = "Get all testimonials (admin view)")
    @GetMapping
    public ResponseEntity<List<TestimonialResponse>> all() {
        return ResponseEntity.ok(testimonialService.all());
    }

    @Operation(summary = "Create a new testimonial")
    @PostMapping
    public ResponseEntity<TestimonialResponse> create(@Valid @RequestBody TestimonialRequest request) {
        return ResponseEntity.ok(testimonialService.create(request));
    }

    @Operation(summary = "Update an existing testimonial")
    @PutMapping("/{id}")
    public ResponseEntity<TestimonialResponse> update(@PathVariable UUID id, @Valid @RequestBody TestimonialRequest request) {
        return ResponseEntity.ok(testimonialService.update(id, request));
    }

    @Operation(summary = "Delete a testimonial")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        testimonialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
