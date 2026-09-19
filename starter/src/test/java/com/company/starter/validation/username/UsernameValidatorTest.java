package com.company.starter.validation.username;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UsernameValidatorTest {
    private final UsernameValidator usernameValidator = new UsernameValidator();

    @Test
    void shouldAlwaysReturnTrue() {
        Assertions.assertTrue(usernameValidator.isValid("username", null));
        Assertions.assertTrue(usernameValidator.isValid(null, null));
    }
}
