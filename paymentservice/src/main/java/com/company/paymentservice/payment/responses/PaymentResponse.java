package com.company.paymentservice.payment.responses;

import com.company.paymentservice.payment.domains.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
    UUID id,
    OrderResponse order,
    PaymentStatus status,
    Instant createdAt,
    Instant approvedAt,
    Instant canceledAt) {

    public record OrderResponse(UUID id) {
    }
}
