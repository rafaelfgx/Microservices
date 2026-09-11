package com.company.customerservice.customer.responses;

import java.util.UUID;

public record CustomerResponse(
    UUID id,
    String name,
    String email,
    String username,
    UUID userId) {
}
