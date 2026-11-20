package com.company.starter.validators.username;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsernameValidator implements ConstraintValidator<Username, String> {
    @Override
    public boolean isValid(final String username, final ConstraintValidatorContext context) {
        return true;
    }
}
