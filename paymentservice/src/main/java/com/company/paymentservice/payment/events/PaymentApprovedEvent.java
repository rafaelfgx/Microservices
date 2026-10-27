package com.company.paymentservice.payment.events;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PaymentApprovedEvent(@NotNull UUID id, @NotNull UUID orderId) {
}
