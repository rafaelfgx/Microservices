package com.company.orderservice.order;

import com.company.orderservice.order.domains.Item;
import com.company.orderservice.order.domains.OrderStatus;
import com.company.orderservice.order.requests.CreateOrderRequest;
import com.company.orderservice.order.requests.ListOrderRequest;
import com.company.orderservice.shared.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.ArrayList;

class OrderMapperTest {
    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    @Test
    void shouldReturnNullWhenCreateOrderRequestIsNull() {
        Assertions.assertThat(mapper.toOrder((CreateOrderRequest) null)).isNull();
    }

    @Test
    void shouldMapToOrderWhenCustomerAndItemsAreNull() {
        final var request = new CreateOrderRequest(null, null);
        final var order = mapper.toOrder(request);
        Assertions.assertThat(order).isNotNull();
        Assertions.assertThat(order.getCustomer()).isNull();
        Assertions.assertThat(order.getItems()).isNull();
    }

    @Test
    void shouldMapToOrderWhenItemAndProductAreNull() {
        final var itemWithNullProduct = new CreateOrderRequest.ItemRequest(null, Data.QUANTITY, Data.PRICE);
        final var items = new ArrayList<CreateOrderRequest.ItemRequest>();
        items.add(itemWithNullProduct);
        items.add(null);
        final var request = new CreateOrderRequest(Data.CUSTOMER_REQUEST, items);
        final var order = mapper.toOrder(request);
        Assertions.assertThat(order).isNotNull();
        Assertions.assertThat(order.getItems()).hasSize(2);
        Assertions.assertThat(order.getItems().getFirst()).isNotNull();
        Assertions.assertThat(order.getItems().getFirst().getProduct()).isNull();
        Assertions.assertThat(order.getItems().get(1)).isNull();
    }

    @Test
    void shouldMapToOrderWhenCreateOrderRequestIsValid() {
        final var order = mapper.toOrder(Data.CREATE_ORDER_REQUEST);
        final var orderCustomer = order.getCustomer();
        final var orderItem = order.getItems().getFirst();
        final var orderProduct = orderItem.getProduct();
        final var orderTotal = order.getItems().stream().map(Item::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        final var orderItemTotal = orderItem.getPrice().multiply(orderItem.getQuantity());

        Assertions.assertThat(order.getId()).isNotNull();
        Assertions.assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(order.getCreatedAt()).isNotNull();
        Assertions.assertThat(order.getCompletedAt()).isNull();
        Assertions.assertThat(order.getCanceledAt()).isNull();
        Assertions.assertThat(order.getTotal()).isEqualTo(orderTotal);

        Assertions.assertThat(orderCustomer.getId()).isEqualTo(Data.CUSTOMER_ID);
        Assertions.assertThat(orderCustomer.getName()).isEqualTo(Data.NAME);

        Assertions.assertThat(orderItem.getId()).isNotNull();
        Assertions.assertThat(orderItem.getQuantity()).isEqualTo(Data.QUANTITY);
        Assertions.assertThat(orderItem.getPrice()).isEqualTo(Data.PRICE);
        Assertions.assertThat(orderItem.getTotal()).isEqualTo(orderItemTotal);

        Assertions.assertThat(orderProduct.getId()).isEqualTo(Data.PRODUCT_ID);
        Assertions.assertThat(orderProduct.getName()).isEqualTo(Data.NAME);
    }

    @Test
    void shouldReturnNullWhenOrderIsNull() {
        Assertions.assertThat(mapper.toCreatedEvent(null)).isNull();
    }

    @Test
    void shouldMapToCreatedEventWhenOrderIsValid() {
        final var event = mapper.toCreatedEvent(Data.ORDER);
        Assertions.assertThat(event).isNotNull();
        Assertions.assertThat(event.id()).isEqualTo(Data.ORDER_ID);
    }

    @Test
    void shouldReturnNullWhenListOrderRequestIsNull() {
        Assertions.assertThat(mapper.toOrder((ListOrderRequest) null)).isNull();
    }

    @Test
    void shouldMapToOrderWhenListOrderRequestIsValid() {
        final var order = mapper.toOrder(Data.LIST_ORDER_REQUEST);
        Assertions.assertThat(order).isNotNull().hasAllNullFieldsOrProperties();
        Assertions.assertThat(order.getTotal()).isEqualTo(BigDecimal.ZERO);
    }
}
