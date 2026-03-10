package com.pryme.Backend.cms;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TestimonialRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 120) String role,
        @NotBlank @Size(max = 1200) String text,
        @Min(1) @Max(5) Integer rating,
        Boolean active,
        Boolean featured,
        Integer displayOrder
) {
}
