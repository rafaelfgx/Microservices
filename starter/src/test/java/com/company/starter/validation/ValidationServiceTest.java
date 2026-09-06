package com.company.starter.validation;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidationServiceTest {
    @Test
    void shouldNotThrowWhenNoViolationsAreFound() {
        final var object = new ValidationInput("valid");

        try (final var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            final var validationService = new ValidationService(validatorFactory.getValidator());
            Assertions.assertDoesNotThrow(() -> validationService.validateOrThrow(object));
        }
    }

    @Test
    void shouldThrowConstraintViolationExceptionWhenViolationsAreFound() {
        final var object = new ValidationInput(" ");

        try (final var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            final var validationService = new ValidationService(validatorFactory.getValidator());
            final var exception = Assertions.assertThrows(ConstraintViolationException.class, () -> validationService.validateOrThrow(object));
            Assertions.assertEquals(1, exception.getConstraintViolations().size());
        }
    }

    private record ValidationInput(@NotBlank String value) {
    }
}
