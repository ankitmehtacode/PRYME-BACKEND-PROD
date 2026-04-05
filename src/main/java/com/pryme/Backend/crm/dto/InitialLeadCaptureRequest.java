package com.pryme.Backend.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class InitialLeadCaptureRequest {
    @NotBlank(message = "Full legal name is required")
    private String fullName;

    @Pattern(regexp = "^\\d{10}$", message = "Invalid mobile number format")
    private String mobileNumber;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Date of birth is required")
    private String dob;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "City is required")
    private String city;

    @Pattern(regexp = "^\\d{6}$", message = "Invalid Pin Code")
    private String pinCode;

    @NotBlank(message = "Loan Type is required")
    private String loanType;

    @NotBlank(message = "Employment Type is required")
    private String employmentType;

    // These are optional because they depend on the employment type
    private String salariedSubType;
    private String professionalSubType;
    private String businessSubType;
}