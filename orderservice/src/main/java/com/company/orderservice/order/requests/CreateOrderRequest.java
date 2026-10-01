package com.company.orderservice.order.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
    @NotNull CustomerRequest customer,
    @NotNull @Size(min = 1) List<@Valid ItemRequest> items) {

    public record CustomerRequest(
        @NotNull UUID id,
        @NotBlank String name) {
    }

    public record ProductRequest(
        @NotNull UUID id,
        @NotBlank String name) {
    }

    public record ItemRequest(
        @NotNull ProductRequest product,
        @NotNull @Positive BigDecimal quantity,
        @NotNull @DecimalMin("0.0") BigDecimal price) {
    }
}
