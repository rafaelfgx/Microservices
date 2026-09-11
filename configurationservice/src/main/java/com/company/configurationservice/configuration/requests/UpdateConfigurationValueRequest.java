package com.company.configurationservice.configuration.requests;

import jakarta.validation.constraints.NotBlank;

public record UpdateConfigurationValueRequest(@NotBlank String id, Object value) {
}
