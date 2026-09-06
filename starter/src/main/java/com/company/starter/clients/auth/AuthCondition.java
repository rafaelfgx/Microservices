package com.company.starter.clients.auth;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class AuthCondition implements Condition {
    @Override
    public boolean matches(final ConditionContext context, final @NonNull AnnotatedTypeMetadata metadata) {
        return Binder.get(context.getEnvironment()).bind("clients.auth", Bindable.of(AuthProperties.class)).isBound();
    }
}
