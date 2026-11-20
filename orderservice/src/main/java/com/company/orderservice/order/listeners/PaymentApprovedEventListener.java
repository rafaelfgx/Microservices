package com.company.orderservice.order.listeners;

import com.company.orderservice.order.events.PaymentApprovedEvent;
import com.company.orderservice.order.handlers.PaymentApprovedEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentApprovedEventListener {
    private final PaymentApprovedEventHandler paymentApprovedEventHandler;

    @KafkaListener(topics = "payments.approved")
    public void listen(final PaymentApprovedEvent event) {
        paymentApprovedEventHandler.handle(event);
    }
}
