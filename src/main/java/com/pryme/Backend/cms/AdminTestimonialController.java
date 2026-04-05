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

    @Operation(summary = "One-line description of this endpoint")
    @GetMapping
    public ResponseEntity<List<TestimonialResponse>> all() {
        return ResponseEntity.ok(testimonialService.all());
    }

    @Operation(summary = "One-line description of this endpoint")
    @PostMapping
    public ResponseEntity<TestimonialResponse> create(@Valid @RequestBody TestimonialRequest request) {
        return ResponseEntity.ok(testimonialService.create(request));
    }

    @Operation(summary = "One-line description of this endpoint")
    @PutMapping("/{id}")
    public ResponseEntity<TestimonialResponse> update(@PathVariable UUID id, @Valid @RequestBody TestimonialRequest request) {
        return ResponseEntity.ok(testimonialService.update(id, request));
    }

    @Operation(summary = "One-line description of this endpoint")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        testimonialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
