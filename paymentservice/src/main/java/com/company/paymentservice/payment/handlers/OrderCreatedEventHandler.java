package com.company.paymentservice.payment.handlers;

import com.company.paymentservice.payment.PaymentMapper;
import com.company.paymentservice.payment.PaymentRepository;
import com.company.paymentservice.payment.events.OrderCreatedEvent;
import com.company.starter.mediator.RequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderCreatedEventHandler implements RequestHandler<OrderCreatedEvent> {
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;

    @Override
    public void handle(final OrderCreatedEvent event) {
        paymentRepository.save(paymentMapper.toPayment(event));
    }
}
