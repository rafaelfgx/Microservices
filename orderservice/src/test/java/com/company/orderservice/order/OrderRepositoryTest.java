package com.company.orderservice.order;

import com.company.orderservice.order.requests.ListOrderRequest;
import com.company.orderservice.order.responses.OrderResponse;
import com.company.orderservice.shared.Data;
import com.company.orderservice.shared.MongoTestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;

@Import(MongoTestConfiguration.class)
@DataMongoTest
class OrderRepositoryTest {
    @Autowired
    MongoTemplate mongo;

    @Autowired
    OrderRepository repository;

    @BeforeEach
    void beforeEach() {
        mongo.getDb().drop();
    }

    @Test
    void shouldReturnEmptyListWhenNoOrdersExist() {
        final var request = new ListOrderRequest(Pageable.unpaged());
        final var orders = repository.findBy(request);
        Assertions.assertThat(orders).isEmpty();
    }

    @Test
    void shouldReturnNonEmptyListWhenOrdersExist() {
        repository.save(Data.ORDER);
        final var request = new ListOrderRequest(Pageable.unpaged());
        final var orders = repository.findBy(request);
        Assertions.assertThat(orders.get().toList().getFirst()).isEqualTo(Data.ORDER_RESPONSE);
    }

    @Test
    void shouldReturnEmptyWhenOrderDoesNotExist() {
        final var order = repository.findById(Data.ORDER_ID, OrderResponse.class);
        Assertions.assertThat(order).isEmpty();
    }

    @Test
    void shouldReturnNonEmptyWhenOrderExists() {
        repository.save(Data.ORDER);
        final var order = repository.findById(Data.ORDER_ID, OrderResponse.class);
        Assertions.assertThat(order).isPresent();
        Assertions.assertThat(order.get()).isEqualTo(Data.ORDER_RESPONSE);
    }
}
