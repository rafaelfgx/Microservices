package com.company.productservice.product.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.With;

import java.util.UUID;

public record UpdateProductRequest(
    @Schema(hidden = true) @With UUID id,
    @NotBlank String name,
    @NotBlank String description) {
}
