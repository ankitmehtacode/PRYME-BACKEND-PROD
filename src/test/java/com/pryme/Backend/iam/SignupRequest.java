package com.pryme.Backend.iam;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @Email @NotBlank(message = "Valid email is required") String email,
        @NotBlank(message = "Password is required") String password
) {}