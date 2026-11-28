package com.company.starter.otp;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "otp")
public record OtpProperties(
    @NotNull @DurationMin(seconds = 1) Duration expiration,
    @Positive int length,
    @Positive int attempts) {
}
