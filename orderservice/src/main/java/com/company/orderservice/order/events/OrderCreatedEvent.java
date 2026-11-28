package com.company.orderservice.order.events;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderCreatedEvent(@NotNull UUID id) {
}
