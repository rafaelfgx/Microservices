package com.company.orderservice.order.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.With;
import org.springframework.data.domain.Pageable;

public record ListOrderRequest(@Schema(hidden = true) @With Pageable pageable) {
}
