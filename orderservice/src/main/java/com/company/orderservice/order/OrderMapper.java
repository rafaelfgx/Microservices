package com.company.orderservice.order;

import com.company.orderservice.order.domains.Order;
import com.company.orderservice.order.events.OrderCreatedEvent;
import com.company.orderservice.order.requests.CreateOrderRequest;
import com.company.orderservice.order.requests.ListOrderRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Order toOrder(final CreateOrderRequest request);

    OrderCreatedEvent toCreatedEvent(final Order order);

    @BeanMapping(builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Order toOrder(final ListOrderRequest request);
}
