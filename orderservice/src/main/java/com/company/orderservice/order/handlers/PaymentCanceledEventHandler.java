package com.company.orderservice.order.handlers;

import com.company.orderservice.order.OrderRepository;
import com.company.orderservice.order.domains.Order;
import com.company.orderservice.order.events.PaymentCanceledEvent;
import com.company.starter.mediator.RequestHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PaymentCanceledEventHandler implements RequestHandler<PaymentCanceledEvent> {
    private final OrderRepository orderRepository;

    @Transactional
    @Override
    public void handle(final PaymentCanceledEvent event) {
        orderRepository.findById(event.orderId()).ifPresent(this::cancel);
    }

    private void cancel(final Order order) {
        order.cancel();
        orderRepository.save(order);
    }
}
