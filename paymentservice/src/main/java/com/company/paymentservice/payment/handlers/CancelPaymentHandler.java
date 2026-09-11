package com.company.paymentservice.payment.handlers;

import com.company.paymentservice.payment.PaymentMapper;
import com.company.paymentservice.payment.PaymentRepository;
import com.company.paymentservice.payment.domains.Payment;
import com.company.paymentservice.payment.requests.CancelPaymentRequest;
import com.company.starter.mediator.RequestResponseHandler;
import com.company.starter.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CancelPaymentHandler implements RequestResponseHandler<CancelPaymentRequest, ResponseEntity<Void>> {
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final OutboxService outboxService;

    @Transactional
    @Override
    public ResponseEntity<Void> handle(final CancelPaymentRequest request) {
        paymentRepository.findById(request.id(), Payment.class).ifPresent(this::cancel);
        return ResponseEntity.noContent().build();
    }

    private void cancel(final Payment payment) {
        payment.cancel();
        paymentRepository.save(payment);
        outboxService.save("payments.canceled", payment.getId().toString(), paymentMapper.toCanceledEvent(payment));
    }
}
