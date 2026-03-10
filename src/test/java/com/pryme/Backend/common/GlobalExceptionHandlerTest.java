package com.pryme.Backend.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void unauthorizedExceptionMapsTo401() {
        ResponseEntity<ApiError> response = handler.handleUnauthorized(new UnauthorizedException("Authentication required"));
        assertEquals(401, response.getStatusCode().value());
        assertEquals("UNAUTHORIZED", response.getBody().code());
    }

    @Test
    void forbiddenExceptionMapsTo403() {
        ResponseEntity<ApiError> response = handler.handleForbidden(new ForbiddenException("No access"));
        assertEquals(403, response.getStatusCode().value());
        assertEquals("FORBIDDEN", response.getBody().code());
    }
}
