package com.company.orderservice.order.handlers;

import com.company.orderservice.order.OrderMapper;
import com.company.orderservice.order.OrderRepository;
import com.company.orderservice.order.requests.CreateOrderRequest;
import com.company.starter.mediator.RequestResponseHandler;
import com.company.starter.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class CreateOrderHandler implements RequestResponseHandler<CreateOrderRequest, ResponseEntity<UUID>> {
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final OutboxService outboxService;

    @Transactional
    @Override
    public ResponseEntity<UUID> handle(final CreateOrderRequest request) {
        final var order = orderRepository.save(orderMapper.toOrder(request));
        outboxService.save("orders.created", order.getId().toString(), orderMapper.toCreatedEvent(order));
        return ResponseEntity.status(HttpStatus.CREATED).body(order.getId());
    }
}
