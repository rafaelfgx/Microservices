package com.company.orderservice.shared;

import com.company.orderservice.order.domains.Customer;
import com.company.orderservice.order.domains.Item;
import com.company.orderservice.order.domains.Order;
import com.company.orderservice.order.domains.OrderStatus;
import com.company.orderservice.order.domains.Product;
import com.company.orderservice.order.events.PaymentApprovedEvent;
import com.company.orderservice.order.events.PaymentCanceledEvent;
import com.company.orderservice.order.requests.CreateOrderRequest;
import com.company.orderservice.order.requests.ListOrderRequest;
import com.company.orderservice.order.responses.OrderResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Data {
    public static final UUID ORDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID CUSTOMER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID PRODUCT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID ITEM_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID PAYMENT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    public static final String NAME = "Name";
    public static final BigDecimal QUANTITY = BigDecimal.valueOf(1);
    public static final BigDecimal PRICE = BigDecimal.valueOf(100);

    public static final Customer CUSTOMER = new Customer(CUSTOMER_ID, NAME);
    public static final OrderResponse.CustomerResponse CUSTOMER_RESPONSE = new OrderResponse.CustomerResponse(CUSTOMER_ID, NAME);
    public static final CreateOrderRequest.CustomerRequest CUSTOMER_REQUEST = new CreateOrderRequest.CustomerRequest(CUSTOMER_ID, NAME);

    public static final Product PRODUCT = new Product(PRODUCT_ID, NAME);
    public static final OrderResponse.ProductResponse PRODUCT_RESPONSE = new OrderResponse.ProductResponse(PRODUCT_ID, NAME);
    public static final CreateOrderRequest.ProductRequest PRODUCT_REQUEST = new CreateOrderRequest.ProductRequest(PRODUCT_ID, NAME);

    public static final Item ITEM = new Item(ITEM_ID, PRODUCT, QUANTITY, PRICE);
    public static final OrderResponse.ItemResponse ITEM_RESPONSE = new OrderResponse.ItemResponse(ITEM_ID, PRODUCT_RESPONSE, QUANTITY, PRICE);
    public static final CreateOrderRequest.ItemRequest ITEM_REQUEST = new CreateOrderRequest.ItemRequest(PRODUCT_REQUEST, QUANTITY, PRICE);

    public static final Order ORDER = new Order(ORDER_ID, CUSTOMER, List.of(ITEM), OrderStatus.CREATED, Instant.EPOCH, null, null);
    public static final OrderResponse ORDER_RESPONSE = new OrderResponse(ORDER_ID, CUSTOMER_RESPONSE, List.of(ITEM_RESPONSE), OrderStatus.CREATED, Instant.EPOCH, null, null);
    public static final CreateOrderRequest CREATE_ORDER_REQUEST = new CreateOrderRequest(CUSTOMER_REQUEST, List.of(ITEM_REQUEST));
    public static final ListOrderRequest LIST_ORDER_REQUEST = new ListOrderRequest(Pageable.unpaged());

    public static final PaymentApprovedEvent PAYMENT_EVENT_APPROVED = new PaymentApprovedEvent(PAYMENT_ID, ORDER_ID);
    public static final PaymentCanceledEvent PAYMENT_EVENT_CANCELED = new PaymentCanceledEvent(PAYMENT_ID, ORDER_ID);
}
