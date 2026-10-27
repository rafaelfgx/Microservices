package com.company.orderservice.order.domains;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Getter
@Setter(AccessLevel.PRIVATE)
public class Item {
    @NotNull
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @NotNull
    private Product product;

    @NotNull
    @Positive
    private BigDecimal quantity;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal price;

    public BigDecimal getTotal() {
        final var safePrice = Objects.requireNonNullElse(price, BigDecimal.ZERO);
        final var safeQuantity = Objects.requireNonNullElse(quantity, BigDecimal.ZERO);
        return safePrice.multiply(safeQuantity);
    }
}
