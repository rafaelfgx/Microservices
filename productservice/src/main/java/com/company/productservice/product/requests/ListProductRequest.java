package com.company.productservice.product.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.With;
import org.springframework.data.domain.Pageable;

public record ListProductRequest(@Schema(hidden = true) @With Pageable pageable, String name) {
}
