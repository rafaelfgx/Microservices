package com.company.configurationservice.configuration.requests;

import jakarta.validation.constraints.NotBlank;

public record CreateConfigurationRequest(@NotBlank String id, Object value, String description, String group) {
}
