package com.company.paymentservice.payment.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GetPaymentByOrderIdRequest(@NotNull UUID orderId) {
}
