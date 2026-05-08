package com.pryme.Backend.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CibilScoreValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCibilScore {
    String message() default "CIBIL Score must be -1 (No Credit History) or between 300 and 900";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
