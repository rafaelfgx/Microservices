package com.company.orderservice.order.handlers;

import com.company.orderservice.order.OrderRepository;
import com.company.orderservice.order.domains.Order;
import com.company.orderservice.order.events.PaymentApprovedEvent;
import com.company.starter.mediator.RequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PaymentApprovedEventHandler implements RequestHandler<PaymentApprovedEvent> {
    private final OrderRepository orderRepository;

    @Transactional
    @Override
    public void handle(final PaymentApprovedEvent event) {
        orderRepository.findById(event.orderId()).ifPresent(this::complete);
    }

    private void complete(final Order order) {
        order.complete();
        orderRepository.save(order);
    }
}
