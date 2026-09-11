package com.company.starter.clients.auth;

import com.company.starter.clients.ClientProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

class AuthPropertiesTest {
    @Test
    void shouldImplementClientProperties() {
        Assertions.assertInstanceOf(ClientProperties.class, new AuthProperties(URI.create("http://localhost"), Duration.ofSeconds(1), Duration.ofSeconds(2)));
    }
}
