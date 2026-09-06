package com.company.orderservice.order.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GetOrderRequest(@NotNull UUID id) {
}
