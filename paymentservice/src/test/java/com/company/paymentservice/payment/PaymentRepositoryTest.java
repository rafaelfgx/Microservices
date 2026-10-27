package com.company.paymentservice.payment;

import com.company.paymentservice.payment.requests.ListPaymentRequest;
import com.company.paymentservice.payment.responses.PaymentResponse;
import com.company.paymentservice.shared.Data;
import com.company.paymentservice.shared.MongoTestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;

@Import(MongoTestConfiguration.class)
@DataMongoTest
class PaymentRepositoryTest {
    @Autowired
    MongoTemplate mongo;

    @Autowired
    PaymentRepository repository;

    @BeforeEach
    void beforeEach() {
        mongo.getDb().drop();
    }

    @Test
    void shouldReturnFalseWhenPaymentDoesNotExistForOrder() {
        Assertions.assertThat(repository.existsByOrderId(Data.ORDER_ID)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenPaymentExistsForOrder() {
        repository.save(Data.PAYMENT);
        Assertions.assertThat(repository.existsByOrderId(Data.ORDER_ID)).isTrue();
    }

    @Test
    void shouldReturnEmptyListWhenNoPaymentsExist() {
        final var request = new ListPaymentRequest(Pageable.unpaged());
        final var payments = repository.findBy(request);
        Assertions.assertThat(payments).isEmpty();
    }

    @Test
    void shouldReturnNonEmptyListWhenPaymentsExist() {
        repository.save(Data.PAYMENT);
        final var request = new ListPaymentRequest(Pageable.unpaged());
        final var payments = repository.findBy(request);
        Assertions.assertThat(payments.get().toList().getFirst()).isEqualTo(Data.PAYMENT_RESPONSE);
    }

    @Test
    void shouldReturnEmptyWhenPaymentDoesNotExist() {
        final var payment = repository.findById(Data.PAYMENT_ID, PaymentResponse.class);
        Assertions.assertThat(payment).isEmpty();
    }

    @Test
    void shouldReturnNonEmptyWhenPaymentExists() {
        repository.save(Data.PAYMENT);
        final var payment = repository.findById(Data.PAYMENT_ID, PaymentResponse.class);
        Assertions.assertThat(payment.orElseThrow()).isEqualTo(Data.PAYMENT_RESPONSE);
    }

    @Test
    void shouldReturnEmptyWhenPaymentDoesNotExistForOrder() {
        final var payment = repository.findByOrderId(Data.ORDER_ID, PaymentResponse.class);
        Assertions.assertThat(payment).isEmpty();
    }

    @Test
    void shouldReturnNonEmptyWhenPaymentExistsForOrder() {
        repository.save(Data.PAYMENT);
        final var payment = repository.findByOrderId(Data.ORDER_ID, PaymentResponse.class);
        Assertions.assertThat(payment.orElseThrow()).isEqualTo(Data.PAYMENT_RESPONSE);
    }
}
