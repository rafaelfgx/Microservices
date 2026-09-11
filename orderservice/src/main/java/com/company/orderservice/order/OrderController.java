package com.company.orderservice.order;

import com.company.orderservice.order.handlers.CreateOrderHandler;
import com.company.orderservice.order.handlers.GetOrderHandler;
import com.company.orderservice.order.handlers.ListOrderHandler;
import com.company.orderservice.order.requests.CreateOrderRequest;
import com.company.orderservice.order.requests.GetOrderRequest;
import com.company.orderservice.order.requests.ListOrderRequest;
import com.company.orderservice.order.responses.OrderResponse;
import com.company.starter.mediator.Mediator;
import com.company.starter.swagger.GetApiResponse;
import com.company.starter.swagger.PostApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Orders")
@RequestMapping("/orders")
@RequiredArgsConstructor
@RestController
public class OrderController {
    private final Mediator mediator;

    @Operation(summary = "List")
    @GetApiResponse
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> list(@ParameterObject @Valid final ListOrderRequest request, @ParameterObject @Valid final Pageable pageable) {
        return mediator.handle(ListOrderHandler.class, request.withPageable(pageable));
    }

    @Operation(summary = "Get")
    @GetApiResponse
    @GetMapping("{id}")
    public ResponseEntity<OrderResponse> get(@PathVariable final UUID id) {
        return mediator.handle(GetOrderHandler.class, new GetOrderRequest(id));
    }

    @Operation(summary = "Create")
    @PostApiResponse
    @PostMapping
    public ResponseEntity<UUID> create(@RequestBody @Valid final CreateOrderRequest request) {
        return mediator.handle(CreateOrderHandler.class, request);
    }
}
