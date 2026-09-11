package com.company.paymentservice.shared;

import com.company.paymentservice.payment.domains.Order;
import com.company.paymentservice.payment.domains.Payment;
import com.company.paymentservice.payment.domains.PaymentStatus;
import com.company.paymentservice.payment.events.OrderCreatedEvent;
import com.company.paymentservice.payment.requests.ListPaymentRequest;
import com.company.paymentservice.payment.responses.PaymentResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Data {
    public static final UUID PAYMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID ORDER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    public static final Order ORDER = new Order(ORDER_ID);
    public static final PaymentResponse.OrderResponse ORDER_RESPONSE = new PaymentResponse.OrderResponse(ORDER_ID);
    public static final OrderCreatedEvent ORDER_EVENT_CREATED = new OrderCreatedEvent(ORDER_ID);
    public static final Payment PAYMENT = new Payment(PAYMENT_ID, ORDER, PaymentStatus.PENDING, Instant.EPOCH, null, null);
    public static final PaymentResponse PAYMENT_RESPONSE = new PaymentResponse(PAYMENT_ID, ORDER_RESPONSE, PaymentStatus.PENDING, Instant.EPOCH, null, null);
    public static final ListPaymentRequest LIST_PAYMENT_REQUEST = new ListPaymentRequest(Pageable.unpaged());
}
