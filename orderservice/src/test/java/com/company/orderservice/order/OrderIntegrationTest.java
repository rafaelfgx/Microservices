package com.company.orderservice.order;

import com.company.orderservice.order.domains.Order;
import com.company.orderservice.order.domains.OrderStatus;
import com.company.orderservice.order.responses.OrderResponse;
import com.company.orderservice.shared.Data;
import com.company.orderservice.shared.SpringBootTestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureMockMvc(addFilters = false)
@Import(SpringBootTestConfiguration.class)
@SpringBootTest(properties = {"spring.kafka.consumer.auto-offset-reset=earliest"})
class OrderIntegrationTest {
    @Autowired
    MockMvcTester mvc;

    @Autowired
    JsonMapper json;

    @Autowired
    KafkaTemplate<String, String> kafka;

    @Autowired
    MongoTemplate mongo;

    @BeforeEach
    void beforeEach() {
        mongo.getDb().drop();
    }

    @Test
    void shouldReturnNotFoundWhenNoOrdersExist() {
        final var result = mvc.get().uri("/orders").exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnOkWhenOrdersExist() {
        mongo.save(Data.ORDER);
        final var result = mvc.get().uri("/orders").exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.OK);
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() {
        mongo.save(Data.ORDER);
        final var result = mvc.get().uri("/orders/{id}", UUID.randomUUID()).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnOkWhenOrderExists() {
        mongo.save(Data.ORDER);
        final var result = mvc.get().uri("/orders/{id}", Data.ORDER_ID).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.OK).bodyJson().convertTo(OrderResponse.class).isEqualTo(Data.ORDER_RESPONSE);
    }

    @Test
    void shouldReturnCreatedWhenCreatingOrder() throws Exception {
        final var result = mvc.post().uri("/orders").contentType(APPLICATION_JSON).content(json.writeValueAsString(Data.CREATE_ORDER_REQUEST)).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.CREATED);
        final var id = json.readValue(result.getMvcResult().getResponse().getContentAsString(), UUID.class);
        Assertions.assertThat(mongo.findOne(Query.query(Criteria.where("id").is(id)), Order.class)).isNotNull();
    }

    @Test
    void shouldCompleteOrderWhenPaymentEventIsApproved() {
        mongo.save(Data.ORDER);
        kafka.send("payments.approved", json.writeValueAsString(Data.PAYMENT_EVENT_APPROVED));
        final var criteria = Criteria.where("id").is(Data.ORDER_ID).and("status").is(OrderStatus.COMPLETED);
        Awaitility.waitAtMost(Duration.ofMinutes(1)).until(() -> mongo.exists(Query.query(criteria), Order.class));
    }

    @Test
    void shouldCancelOrderWhenPaymentEventIsCanceled() {
        mongo.save(Data.ORDER);
        kafka.send("payments.canceled", json.writeValueAsString(Data.PAYMENT_EVENT_CANCELED));
        final var criteria = Criteria.where("id").is(Data.ORDER_ID).and("status").is(OrderStatus.CANCELED);
        Awaitility.waitAtMost(Duration.ofMinutes(1)).until(() -> mongo.exists(Query.query(criteria), Order.class));
    }
}
