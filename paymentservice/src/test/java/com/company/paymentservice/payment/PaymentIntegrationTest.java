package com.company.paymentservice.payment;

import com.company.paymentservice.payment.domains.Payment;
import com.company.paymentservice.payment.domains.PaymentStatus;
import com.company.paymentservice.payment.responses.PaymentResponse;
import com.company.paymentservice.shared.Data;
import com.company.paymentservice.shared.SpringBootTestConfiguration;
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

@AutoConfigureMockMvc(addFilters = false)
@Import(SpringBootTestConfiguration.class)
@SpringBootTest(properties = {"spring.kafka.consumer.auto-offset-reset=earliest"})
class PaymentIntegrationTest {
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
    void shouldReturnNotFoundWhenNoPaymentsExist() {
        final var result = mvc.get().uri("/payments").exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnOkWhenPaymentsExist() {
        mongo.save(Data.PAYMENT);
        final var result = mvc.get().uri("/payments").exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.OK);
    }

    @Test
    void shouldReturnNotFoundWhenPaymentDoesNotExist() {
        mongo.save(Data.PAYMENT);
        final var result = mvc.get().uri("/payments/{id}", UUID.randomUUID()).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnOkWhenPaymentExists() {
        mongo.save(Data.PAYMENT);
        final var result = mvc.get().uri("/payments/{id}", Data.PAYMENT_ID).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.OK).bodyJson().convertTo(PaymentResponse.class).isEqualTo(Data.PAYMENT_RESPONSE);
    }

    @Test
    void shouldReturnNotFoundWhenPaymentDoesNotExistForOrder() {
        mongo.save(Data.PAYMENT);
        final var result = mvc.get().uri("/payments/order/{orderId}", UUID.randomUUID()).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnOkWhenPaymentExistsForOrder() {
        mongo.save(Data.PAYMENT);
        final var result = mvc.get().uri("/payments/order/{orderId}", Data.ORDER_ID).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.OK).bodyJson().convertTo(PaymentResponse.class).isEqualTo(Data.PAYMENT_RESPONSE);
    }

    @Test
    void shouldReturnNoContentWhenApprovingPayment() {
        mongo.save(Data.PAYMENT);
        final var result = mvc.patch().uri("/payments/{id}/approve", Data.PAYMENT_ID).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        Assertions.assertThat(mongo.findOne(Query.query(Criteria.where("id").is(Data.PAYMENT_ID)), Payment.class)).isNotNull();
    }

    @Test
    void shouldReturnNoContentWhenCancelingPayment() {
        mongo.save(Data.PAYMENT);
        final var result = mvc.patch().uri("/payments/{id}/cancel", Data.PAYMENT_ID).exchange();
        Assertions.assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        Assertions.assertThat(mongo.findOne(Query.query(Criteria.where("id").is(Data.PAYMENT_ID)), Payment.class)).isNotNull();
    }

    @Test
    void shouldCreatePaymentWhenOrderCreatedEventIsSent() {
        kafka.send("orders.created", json.writeValueAsString(Data.ORDER_EVENT_CREATED));
        final var criteria = Criteria.where("order.id").is(Data.ORDER_ID).and("status").is(PaymentStatus.PENDING);
        Awaitility.waitAtMost(Duration.ofMinutes(1)).until(() -> mongo.exists(Query.query(criteria), Payment.class));
    }
}
