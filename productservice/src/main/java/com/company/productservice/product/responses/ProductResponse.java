package com.company.productservice.product.responses;

import java.util.UUID;

public record ProductResponse(
    UUID id,
    String name,
    String description) {
}
