package com.company.orderservice.order.events;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PaymentApprovedEvent(@NotNull UUID id, @NotNull UUID orderId) {
}
