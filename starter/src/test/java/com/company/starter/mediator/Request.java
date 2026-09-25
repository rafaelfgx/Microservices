package com.company.starter.mediator;

import jakarta.validation.constraints.NotBlank;

public record Request(@NotBlank String message) {
}
