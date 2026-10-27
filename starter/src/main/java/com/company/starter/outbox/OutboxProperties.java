package com.company.starter.outbox;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "outbox")
public record OutboxProperties(
    @NotNull @DurationMin(seconds = 1) Duration delay,
    @NotNull @DurationMin(seconds = 1) Duration lock,
    @Positive int limit,
    @NotNull @DurationMin(seconds = 1) Duration retryInitialDelay,
    @NotNull @DurationMin(seconds = 1) Duration retryMaxDelay,
    @DecimalMin("1.0") double retryMultiplier) {
}
