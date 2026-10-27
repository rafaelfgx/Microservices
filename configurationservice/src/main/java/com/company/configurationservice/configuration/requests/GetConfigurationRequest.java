package com.company.configurationservice.configuration.requests;

import jakarta.validation.constraints.NotNull;

public record GetConfigurationRequest(@NotNull String id) {
}
