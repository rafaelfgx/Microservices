package com.company.starter.otp;

import jakarta.validation.constraints.NotBlank;

public record GenerateOtpRequest(@NotBlank String type, @NotBlank String key) {
}
