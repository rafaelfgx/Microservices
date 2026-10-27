package com.company.orderservice.order.domains;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Getter
@Setter(AccessLevel.PRIVATE)
@Document("orders")
public class Order {
    @Id
    @NotNull
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @NotNull
    private Customer customer;

    @NotNull
    @Size(min = 1)
    private List<@Valid Item> items;

    @NotNull
    @Builder.Default
    private OrderStatus status = OrderStatus.CREATED;

    @NotNull
    @PastOrPresent
    @Builder.Default
    private Instant createdAt = Instant.now();

    @PastOrPresent
    private Instant completedAt;

    @PastOrPresent
    private Instant canceledAt;

    public BigDecimal getTotal() {
        return Objects
            .requireNonNullElse(items, List.<Item>of())
            .stream()
            .filter(Objects::nonNull)
            .map(Item::getTotal)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void complete() {
        this.status = OrderStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void cancel() {
        this.status = OrderStatus.CANCELED;
        this.canceledAt = Instant.now();
    }
}
