package com.company.orderservice.order.responses;

import com.company.orderservice.order.domains.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    CustomerResponse customer,
    List<ItemResponse> items,
    OrderStatus status,
    Instant createdAt,
    Instant completedAt,
    Instant canceledAt) {

    public record CustomerResponse(
        UUID id,
        String name) {
    }

    public record ProductResponse(
        UUID id,
        String name) {
    }

    public record ItemResponse(
        UUID id,
        ProductResponse product,
        BigDecimal quantity,
        BigDecimal price) {
    }
}
