package com.company.paymentservice.payment.handlers;

import com.company.paymentservice.payment.PaymentMapper;
import com.company.paymentservice.payment.PaymentRepository;
import com.company.paymentservice.payment.domains.Payment;
import com.company.paymentservice.payment.requests.ApprovePaymentRequest;
import com.company.starter.mediator.RequestResponseHandler;
import com.company.starter.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class ApprovePaymentHandler implements RequestResponseHandler<ApprovePaymentRequest, ResponseEntity<Void>> {
    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final OutboxService outboxService;

    @Transactional
    @Override
    public ResponseEntity<Void> handle(final ApprovePaymentRequest request) {
        paymentRepository.findById(request.id(), Payment.class).ifPresent(this::approve);
        return ResponseEntity.noContent().build();
    }

    private void approve(final Payment payment) {
        payment.approve();
        paymentRepository.save(payment);
        outboxService.save("payments.approved", payment.getId().toString(), paymentMapper.toApprovedEvent(payment));
    }
}
