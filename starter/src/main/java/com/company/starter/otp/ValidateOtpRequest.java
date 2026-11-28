package com.company.starter.otp;

import jakarta.validation.constraints.NotBlank;

public record ValidateOtpRequest(@NotBlank String type, @NotBlank String key, @NotBlank String code) {
}
