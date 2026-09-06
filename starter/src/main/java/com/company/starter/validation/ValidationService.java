package com.company.starter.validation;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidationService {
    private final Validator validator;

    public <T> void validateOrThrow(final T object) {
        final var violations = validator.validate(object);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
    }
}
