package com.pryme.Backend.common;

public record ApiError(
        String code,
        String message
) {
}
