package com.pryme.Backend.iam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 🧠 SILICON-VALLEY FIX: Prevents Jackson from throwing a 500 error when React sends extra UI variables
@JsonIgnoreProperties(ignoreUnknown = true)
public record SignupRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @Email @NotBlank(message = "Valid email is required") String email,
        @NotBlank(message = "Password is required") String password
) {}