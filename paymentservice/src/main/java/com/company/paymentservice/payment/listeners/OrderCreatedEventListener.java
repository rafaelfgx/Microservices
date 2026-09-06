package com.company.paymentservice.payment.listeners;

import com.company.paymentservice.payment.events.OrderCreatedEvent;
import com.company.paymentservice.payment.handlers.OrderCreatedEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Component
public class OrderCreatedEventListener {
    private final JsonMapper json;
    private final OrderCreatedEventHandler orderCreatedEventHandler;

    @KafkaListener(topics = "orders.created")
    public void listen(final String data) {
        orderCreatedEventHandler.handle(json.readValue(data, OrderCreatedEvent.class));
    }
}
