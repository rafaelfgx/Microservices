package com.company.paymentservice.payment;

import com.company.paymentservice.payment.domains.Order;
import com.company.paymentservice.payment.domains.Payment;
import com.company.paymentservice.payment.events.OrderCreatedEvent;
import com.company.paymentservice.payment.events.PaymentApprovedEvent;
import com.company.paymentservice.payment.events.PaymentCanceledEvent;
import com.company.paymentservice.payment.requests.ListPaymentRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", source = ".")
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Payment toPayment(final OrderCreatedEvent event);

    Order toOrder(final OrderCreatedEvent event);

    @Mapping(source = "order.id", target = "orderId")
    PaymentApprovedEvent toApprovedEvent(final Payment payment);

    @Mapping(source = "order.id", target = "orderId")
    PaymentCanceledEvent toCanceledEvent(final Payment payment);

    @BeanMapping(builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Payment toPayment(final ListPaymentRequest request);
}
