package com.company.paymentservice.payment;

import com.company.paymentservice.payment.handlers.ApprovePaymentHandler;
import com.company.paymentservice.payment.handlers.CancelPaymentHandler;
import com.company.paymentservice.payment.handlers.GetPaymentByOrderIdHandler;
import com.company.paymentservice.payment.handlers.GetPaymentHandler;
import com.company.paymentservice.payment.handlers.ListPaymentHandler;
import com.company.paymentservice.payment.requests.ApprovePaymentRequest;
import com.company.paymentservice.payment.requests.CancelPaymentRequest;
import com.company.paymentservice.payment.requests.GetPaymentByOrderIdRequest;
import com.company.paymentservice.payment.requests.GetPaymentRequest;
import com.company.paymentservice.payment.requests.ListPaymentRequest;
import com.company.paymentservice.payment.responses.PaymentResponse;
import com.company.starter.mediator.Mediator;
import com.company.starter.swagger.DefaultApiResponse;
import com.company.starter.swagger.GetApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Payments")
@RequestMapping("/payments")
@RequiredArgsConstructor
@RestController
public class PaymentController {
    private final Mediator mediator;

    @Operation(summary = "List")
    @GetApiResponse
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> list(@ParameterObject @Valid final ListPaymentRequest request, @ParameterObject @Valid final Pageable pageable) {
        return mediator.handle(ListPaymentHandler.class, request.withPageable(pageable));
    }

    @Operation(summary = "Get")
    @GetApiResponse
    @GetMapping("{id}")
    public ResponseEntity<PaymentResponse> get(@PathVariable final UUID id) {
        return mediator.handle(GetPaymentHandler.class, new GetPaymentRequest(id));
    }

    @Operation(summary = "Get By Order Id")
    @GetApiResponse
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getByOrderId(@PathVariable final UUID orderId) {
        return mediator.handle(GetPaymentByOrderIdHandler.class, new GetPaymentByOrderIdRequest(orderId));
    }

    @Operation(summary = "Approve")
    @DefaultApiResponse
    @PatchMapping("{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable final UUID id) {
        return mediator.handle(ApprovePaymentHandler.class, new ApprovePaymentRequest(id));
    }

    @Operation(summary = "Cancel")
    @DefaultApiResponse
    @PatchMapping("{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable final UUID id) {
        return mediator.handle(CancelPaymentHandler.class, new CancelPaymentRequest(id));
    }
}
