package com.company.starter.validation.password;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PasswordValidatorTest {
    private final PasswordValidator passwordValidator = new PasswordValidator();

    @Test
    void shouldAlwaysReturnTrue() {
        Assertions.assertTrue(passwordValidator.isValid("password", null));
        Assertions.assertTrue(passwordValidator.isValid(null, null));
    }
}
