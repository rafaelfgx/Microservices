package com.company.paymentservice.payment.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.With;
import org.springframework.data.domain.Pageable;

public record ListPaymentRequest(@Schema(hidden = true) @With Pageable pageable) {
}
