package com.pryme.Backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CibilScoreValidator implements ConstraintValidator<ValidCibilScore, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return false; // Typically handled by @NotNull if nullability is not allowed
        }
        return value == -1 || (value >= 300 && value <= 900);
    }
}
