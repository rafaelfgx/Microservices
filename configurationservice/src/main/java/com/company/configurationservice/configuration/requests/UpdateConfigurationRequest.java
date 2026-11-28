package com.company.configurationservice.configuration.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.With;

public record UpdateConfigurationRequest(@Schema(hidden = true) @With String id, Object value, String description, String group) {
}
