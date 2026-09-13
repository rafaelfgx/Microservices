package com.company.productservice.product.requests;

import jakarta.validation.constraints.NotBlank;

public record CreateProductRequest(
    @NotBlank String name,
    @NotBlank String description) {
}
