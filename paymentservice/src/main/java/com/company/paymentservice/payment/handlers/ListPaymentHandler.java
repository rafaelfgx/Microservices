package com.company.paymentservice.payment.handlers;

import com.company.paymentservice.payment.PaymentRepository;
import com.company.paymentservice.payment.requests.ListPaymentRequest;
import com.company.paymentservice.payment.responses.PaymentResponse;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ListPaymentHandler implements RequestResponseHandler<ListPaymentRequest, ResponseEntity<Page<PaymentResponse>>> {
    private final PaymentRepository paymentRepository;

    @Override
    public ResponseEntity<Page<PaymentResponse>> handle(final ListPaymentRequest request) {
        return ResponseEntity.of(Optional.of(paymentRepository.findBy(request)).filter(Page::hasContent));
    }
}
