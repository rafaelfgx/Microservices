package com.company.customerservice.customer;

import com.company.customerservice.customer.responses.CustomerResponse;
import com.company.customerservice.shared.Data;
import com.company.customerservice.shared.MongoTestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@Import(MongoTestConfiguration.class)
@DataMongoTest
class CustomerRepositoryTest {
    @Autowired
    MongoTemplate mongo;

    @Autowired
    CustomerRepository repository;

    @BeforeEach
    void beforeEach() {
        mongo.getDb().drop();
    }

    @Test
    void shouldReturnFalseWhenCustomerDoesNotExist() {
        Assertions.assertThat(repository.existsByEmailOrUsername(Data.EMAIL, Data.USERNAME)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenCustomerExists() {
        repository.save(Data.CUSTOMER);
        Assertions.assertThat(repository.existsByEmailOrUsername(Data.EMAIL, Data.USERNAME)).isTrue();
    }

    @Test
    void shouldReturnEmptyListWhenNoCustomersExist() {
        final var customers = repository.findBy(Data.LIST_CUSTOMER_REQUEST);
        Assertions.assertThat(customers).isEmpty();
    }

    @Test
    void shouldReturnNonEmptyListWhenCustomersExist() {
        repository.save(Data.CUSTOMER);
        final var customers = repository.findBy(Data.LIST_CUSTOMER_REQUEST);
        Assertions.assertThat(customers.get().toList().getFirst()).isEqualTo(Data.CUSTOMER_RESPONSE);
    }

    @Test
    void shouldReturnEmptyWhenCustomerDoesNotExist() {
        final var customer = repository.findById(Data.CUSTOMER.getId(), CustomerResponse.class);
        Assertions.assertThat(customer).isEmpty();
    }

    @Test
    void shouldReturnNonEmptyWhenCustomerExists() {
        repository.save(Data.CUSTOMER);
        final var customer = repository.findById(Data.CUSTOMER.getId(), CustomerResponse.class);
        Assertions.assertThat(customer).isPresent();
        Assertions.assertThat(customer.get()).isEqualTo(Data.CUSTOMER_RESPONSE);
    }
}
