package com.company.orderservice.order.handlers;

import com.company.orderservice.order.OrderRepository;
import com.company.orderservice.order.requests.ListOrderRequest;
import com.company.orderservice.order.responses.OrderResponse;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ListOrderHandler implements RequestResponseHandler<ListOrderRequest, ResponseEntity<Page<OrderResponse>>> {
    private final OrderRepository orderRepository;

    @Override
    public ResponseEntity<Page<OrderResponse>> handle(final ListOrderRequest request) {
        return ResponseEntity.of(Optional.of(orderRepository.findBy(request)).filter(Page::hasContent));
    }
}
