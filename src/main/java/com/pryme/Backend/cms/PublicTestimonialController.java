package com.pryme.Backend.cms;

import io.swagger.v3.oas.annotations.Operation;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/reviews")
@RequiredArgsConstructor
public class PublicTestimonialController {

    private final TestimonialService testimonialService;

    @Operation(summary = "One-line description of this endpoint")
    @GetMapping
    @Timed(value = "pryme.api.public.reviews", description = "Public reviews endpoint latency")
    public ResponseEntity<Map<String, List<TestimonialResponse>>> reviews() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(Map.of("reviews", testimonialService.publicTestimonials()));
    }
}
