package com.company.paymentservice.payment;

import com.company.paymentservice.payment.domains.Payment;
import com.company.paymentservice.payment.events.OrderCreatedEvent;
import com.company.paymentservice.payment.requests.ListPaymentRequest;
import com.company.paymentservice.shared.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PaymentMapperTest {
    private final PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);

    @Test
    void shouldReturnNullWhenOrderCreatedEventIsNull() {
        Assertions.assertThat(mapper.toPayment((OrderCreatedEvent) null)).isNull();
    }

    @Test
    void shouldMapToPaymentWhenOrderCreatedEventIsValid() {
        final var payment = mapper.toPayment(Data.ORDER_EVENT_CREATED);
        Assertions.assertThat(payment).isNotNull();
        Assertions.assertThat(payment.getId()).isNotNull();
        Assertions.assertThat(payment.getId()).isNotEqualTo(Data.ORDER_ID);
        Assertions.assertThat(payment.getOrder()).isNotNull();
        Assertions.assertThat(payment.getOrder().getId()).isEqualTo(Data.ORDER_ID);
        Assertions.assertThat(payment.getStatus()).isEqualTo(Data.PAYMENT.getStatus());
        Assertions.assertThat(payment.getCreatedAt()).isNotNull();
        Assertions.assertThat(payment.getApprovedAt()).isNull();
        Assertions.assertThat(payment.getCanceledAt()).isNull();
    }

    @Test
    void shouldReturnNullWhenOrderCreatedEventToOrderIsNull() {
        Assertions.assertThat(mapper.toOrder(null)).isNull();
    }

    @Test
    void shouldReturnNullWhenPaymentApprovedEventIsNull() {
        Assertions.assertThat(mapper.toApprovedEvent(null)).isNull();
    }

    @Test
    void shouldMapToPaymentApprovedEventWhenOrderIsNull() {
        final var event = mapper.toApprovedEvent(Payment.builder().order(null).build());
        Assertions.assertThat(event).isNotNull();
        Assertions.assertThat(event.orderId()).isNull();
    }

    @Test
    void shouldMapToPaymentApprovedEventWhenValid() {
        final var event = mapper.toApprovedEvent(Data.PAYMENT);
        Assertions.assertThat(event).isNotNull();
        Assertions.assertThat(event.id()).isEqualTo(Data.PAYMENT_ID);
        Assertions.assertThat(event.orderId()).isEqualTo(Data.ORDER_ID);
    }

    @Test
    void shouldReturnNullWhenPaymentCanceledEventIsNull() {
        Assertions.assertThat(mapper.toCanceledEvent(null)).isNull();
    }

    @Test
    void shouldMapToPaymentCanceledEventWhenOrderIsNull() {
        final var event = mapper.toCanceledEvent(Payment.builder().order(null).build());
        Assertions.assertThat(event).isNotNull();
        Assertions.assertThat(event.orderId()).isNull();
    }

    @Test
    void shouldMapToPaymentCanceledEventWhenValid() {
        final var event = mapper.toCanceledEvent(Data.PAYMENT);
        Assertions.assertThat(event).isNotNull();
        Assertions.assertThat(event.id()).isEqualTo(Data.PAYMENT_ID);
        Assertions.assertThat(event.orderId()).isEqualTo(Data.ORDER_ID);
    }

    @Test
    void shouldReturnNullWhenListPaymentRequestIsNull() {
        Assertions.assertThat(mapper.toPayment((ListPaymentRequest) null)).isNull();
    }

    @Test
    void shouldMapToPaymentWhenListPaymentRequestIsValid() {
        final var payment = mapper.toPayment(Data.LIST_PAYMENT_REQUEST);
        Assertions.assertThat(payment).isNotNull().hasAllNullFieldsOrProperties();
    }
}
