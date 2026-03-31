package com.pryme.Backend.cms;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestimonialResponse(
        UUID id,
        String name,
        String role,
        String text,
        Integer rating,
        boolean active,
        boolean featured,
        Integer displayOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TestimonialResponse from(Testimonial t) {
        return new TestimonialResponse(
                t.getId(), t.getName(), t.getRole(), t.getText(), t.getRating(),
                t.getActive(), t.getFeatured(), t.getDisplayOrder(), t.getCreatedAt(), t.getUpdatedAt()
        );
    }
}
