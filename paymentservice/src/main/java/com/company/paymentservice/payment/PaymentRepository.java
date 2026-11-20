package com.company.paymentservice.payment;

import com.company.paymentservice.payment.domains.Payment;
import com.company.paymentservice.payment.requests.ListPaymentRequest;
import com.company.paymentservice.payment.responses.PaymentResponse;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.domain.ExampleMatcher.StringMatcher.CONTAINING;
import static org.springframework.data.domain.ExampleMatcher.matching;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, UUID> {
    ExampleMatcher matcher = matching().withIgnoreNullValues().withStringMatcher(CONTAINING).withIgnoreCase();
    PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);

    boolean existsByOrderId(final UUID orderId);

    <T> Optional<T> findById(final UUID id, final Class<T> type);

    <T> Optional<T> findByOrderId(final UUID orderId, final Class<T> type);

    default Page<PaymentResponse> findBy(final ListPaymentRequest request) {
        return findBy(Example.of(mapper.toPayment(request), matcher), query -> query.as(PaymentResponse.class).page(request.pageable()));
    }
}
