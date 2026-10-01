package com.company.orderservice.order.handlers;

import com.company.orderservice.order.OrderRepository;
import com.company.orderservice.order.requests.GetOrderRequest;
import com.company.orderservice.order.responses.OrderResponse;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetOrderHandler implements RequestResponseHandler<GetOrderRequest, ResponseEntity<OrderResponse>> {
    private final OrderRepository orderRepository;

    @Override
    public ResponseEntity<OrderResponse> handle(final GetOrderRequest request) {
        return ResponseEntity.of(orderRepository.findById(request.id(), OrderResponse.class));
    }
}
