package com.company.paymentservice.payment.events;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderCreatedEvent(@NotNull UUID id) {
}
