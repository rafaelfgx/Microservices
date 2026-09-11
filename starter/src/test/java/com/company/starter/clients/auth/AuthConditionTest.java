package com.company.starter.clients.auth;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

class AuthConditionTest {
    final AuthCondition condition = new AuthCondition();
    final AnnotatedTypeMetadata metadata = Mockito.mock(AnnotatedTypeMetadata.class);

    @Test
    void shouldMatchWhenClientsAuthIsConfigured() {
        final var environment = new MockEnvironment();
        environment.setProperty("clients.auth.url", "http://localhost:8080");
        final var context = Mockito.mock(ConditionContext.class);
        Mockito.when(context.getEnvironment()).thenReturn(environment);
        Assertions.assertTrue(condition.matches(context, metadata));
    }

    @Test
    void shouldNotMatchWhenClientsAuthIsNotConfigured() {
        final var environment = new MockEnvironment();
        final var context = Mockito.mock(ConditionContext.class);
        Mockito.when(context.getEnvironment()).thenReturn(environment);
        Assertions.assertFalse(condition.matches(context, metadata));
    }
}
