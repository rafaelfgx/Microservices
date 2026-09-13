package com.company.paymentservice.payment.handlers;

import com.company.paymentservice.payment.PaymentRepository;
import com.company.paymentservice.payment.requests.GetPaymentRequest;
import com.company.paymentservice.payment.responses.PaymentResponse;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetPaymentHandler implements RequestResponseHandler<GetPaymentRequest, ResponseEntity<PaymentResponse>> {
    private final PaymentRepository paymentRepository;

    @Override
    public ResponseEntity<PaymentResponse> handle(final GetPaymentRequest request) {
        return ResponseEntity.of(paymentRepository.findById(request.id(), PaymentResponse.class));
    }
}
