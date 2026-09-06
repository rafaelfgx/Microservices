package com.company.customerservice.customer.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.With;

import java.util.UUID;

public record UpdateCustomerRequest(
    @Schema(hidden = true) @With UUID id,
    @NotBlank String name) {
}
