package com.company.orderservice.order.listeners;

import com.company.orderservice.order.events.PaymentCanceledEvent;
import com.company.orderservice.order.handlers.PaymentCanceledEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentCanceledEventListener {
    private final PaymentCanceledEventHandler paymentCanceledEventHandler;

    @KafkaListener(topics = "payments.canceled")
    public void listen(final PaymentCanceledEvent event) {
        paymentCanceledEventHandler.handle(event);
    }
}
