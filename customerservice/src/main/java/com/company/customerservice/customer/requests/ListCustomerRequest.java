package com.company.customerservice.customer.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.With;
import org.springframework.data.domain.Pageable;

public record ListCustomerRequest(
    @Schema(hidden = true) @With Pageable pageable,
    String name,
    String email,
    String username) {
}
