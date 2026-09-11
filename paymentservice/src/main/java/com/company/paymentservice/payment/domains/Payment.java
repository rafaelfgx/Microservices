package com.company.paymentservice.payment.domains;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Getter
@Setter(AccessLevel.PRIVATE)
@CompoundIndex(def = "{'order.id': 1}")
@Document("payments")
public class Payment {
    @Id
    @NotNull
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @NotNull
    private Order order;

    @NotNull
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @NotNull
    @PastOrPresent
    @Builder.Default
    private Instant createdAt = Instant.now();

    @PastOrPresent
    private Instant approvedAt;

    @PastOrPresent
    private Instant canceledAt;

    public void approve() {
        this.status = PaymentStatus.APPROVED;
        this.approvedAt = Instant.now();
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = Instant.now();
    }
}
