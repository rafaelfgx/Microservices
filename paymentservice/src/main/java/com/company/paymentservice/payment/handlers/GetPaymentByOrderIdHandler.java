package com.company.paymentservice.payment.handlers;

import com.company.paymentservice.payment.PaymentRepository;
import com.company.paymentservice.payment.requests.GetPaymentByOrderIdRequest;
import com.company.paymentservice.payment.responses.PaymentResponse;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetPaymentByOrderIdHandler implements RequestResponseHandler<GetPaymentByOrderIdRequest, ResponseEntity<PaymentResponse>> {
    private final PaymentRepository paymentRepository;

    @Override
    public ResponseEntity<PaymentResponse> handle(final GetPaymentByOrderIdRequest request) {
        return ResponseEntity.of(paymentRepository.findByOrderId(request.orderId(), PaymentResponse.class));
    }
}
