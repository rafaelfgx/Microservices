package com.company.starter.cryptography;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncoderTest {
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Test
    void shouldMatchWhenRawPasswordMatchesEncodedPassword() {
        Assertions.assertTrue(passwordEncoder.matches("password", passwordEncoder.encode("password")));
    }
}
