package com.company.productservice.product.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteProductRequest(@NotNull UUID id) {
}
