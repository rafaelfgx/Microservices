package com.company.customerservice.customer.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GetCustomerRequest(@NotNull UUID id) {
}
