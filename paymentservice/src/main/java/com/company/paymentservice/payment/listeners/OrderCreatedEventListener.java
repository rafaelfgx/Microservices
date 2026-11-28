package com.company.paymentservice.payment.listeners;

import com.company.paymentservice.payment.events.OrderCreatedEvent;
import com.company.paymentservice.payment.handlers.OrderCreatedEventHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderCreatedEventListener {
    private final OrderCreatedEventHandler orderCreatedEventHandler;

    @KafkaListener(topics = "orders.created")
    public void listen(@Payload @Valid final OrderCreatedEvent event) {
        orderCreatedEventHandler.handle(event);
    }
}
