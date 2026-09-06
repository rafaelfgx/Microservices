package com.company.customerservice.customer;

import com.company.customerservice.customer.requests.UpdateCustomerRequest;
import com.company.customerservice.shared.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

class CustomerMapperTest {
    private final CustomerMapper mapper = Mappers.getMapper(CustomerMapper.class);

    @Test
    void shouldReturnNullWhenCreateCustomerRequestAndUserIdAreNull() {
        Assertions.assertThat(mapper.toCustomer(null, (UUID) null)).isNull();
    }

    @Test
    void shouldMapToCustomerWhenCreateCustomerRequestIsNull() {
        final var customer = mapper.toCustomer(null, Data.USER_ID);
        Assertions.assertThat(customer).isNotNull();
        Assertions.assertThat(customer).hasAllNullFieldsOrPropertiesExcept("id", "userId");
        Assertions.assertThat(customer.getId()).isNotNull();
        Assertions.assertThat(customer.getUserId()).isEqualTo(Data.USER_ID);
    }

    @Test
    void shouldMapToCustomerWhenUserIdIsNull() {
        final var customer = mapper.toCustomer(Data.CREATE_CUSTOMER_REQUEST, null);
        Assertions.assertThat(customer).isNotNull();
        Assertions.assertThat(customer.getId()).isNotNull();
        Assertions.assertThat(customer.getUserId()).isNull();
        Assertions.assertThat(customer.getName()).isEqualTo(Data.NAME);
        Assertions.assertThat(customer.getEmail()).isEqualTo(Data.EMAIL);
        Assertions.assertThat(customer.getUsername()).isEqualTo(Data.USERNAME);
    }

    @Test
    void shouldMapToCustomerWhenCreateCustomerRequestAndUserIdAreValid() {
        final var customer = mapper.toCustomer(Data.CREATE_CUSTOMER_REQUEST, Data.USER_ID);
        Assertions.assertThat(customer).isNotNull();
        Assertions.assertThat(customer.getId()).isNotNull();
        Assertions.assertThat(customer.getUserId()).isNotNull();
        Assertions.assertThat(customer.getName()).isEqualTo(Data.NAME);
        Assertions.assertThat(customer.getEmail()).isEqualTo(Data.EMAIL);
        Assertions.assertThat(customer.getUsername()).isEqualTo(Data.USERNAME);
    }

    @Test
    void shouldReturnNullWhenCustomerAndUpdateCustomerRequestAreNull() {
        Assertions.assertThat(mapper.toCustomer(null, (UpdateCustomerRequest) null)).isNull();
    }

    @Test
    void shouldMapToCustomerWhenCustomerIsNull() {
        final var customer = mapper.toCustomer(null, Data.UPDATE_CUSTOMER_REQUEST);
        Assertions.assertThat(customer).isNotNull();
        Assertions.assertThat(customer).hasAllNullFieldsOrPropertiesExcept("id", "name");
        Assertions.assertThat(customer.getId()).isNotNull();
        Assertions.assertThat(customer.getName()).isEqualTo(Data.NAME_UPDATED);
    }

    @Test
    void shouldMapToCustomerWhenUpdateCustomerRequestIsNull() {
        final var customer = mapper.toCustomer(Data.CUSTOMER, null);
        Assertions.assertThat(customer).isNotNull();
        Assertions.assertThat(customer.getId()).isEqualTo(Data.CUSTOMER.getId());
        Assertions.assertThat(customer.getUserId()).isEqualTo(Data.CUSTOMER.getUserId());
        Assertions.assertThat(customer.getName()).isNull();
        Assertions.assertThat(customer.getEmail()).isEqualTo(Data.CUSTOMER.getEmail());
        Assertions.assertThat(customer.getUsername()).isEqualTo(Data.CUSTOMER.getUsername());
    }

    @Test
    void shouldMapToCustomerWhenCustomerAndUpdateCustomerRequestAreValid() {
        final var customer = mapper.toCustomer(Data.CUSTOMER, Data.UPDATE_CUSTOMER_REQUEST);
        Assertions.assertThat(customer).isNotNull();
        Assertions.assertThat(customer.getId()).isNotNull();
        Assertions.assertThat(customer.getEmail()).isNotNull();
        Assertions.assertThat(customer.getUsername()).isNotNull();
        Assertions.assertThat(customer.getUserId()).isNotNull();
        Assertions.assertThat(customer.getName()).isEqualTo(Data.NAME_UPDATED);
    }

    @Test
    void shouldReturnNullWhenListCustomerRequestIsNull() {
        Assertions.assertThat(mapper.toCustomer(null)).isNull();
    }

    @Test
    void shouldMapToCustomerWhenListCustomerRequestIsValid() {
        final var customer = mapper.toCustomer(Data.LIST_CUSTOMER_REQUEST);
        Assertions.assertThat(customer).isNotNull();
        Assertions.assertThat(customer).hasAllNullFieldsOrPropertiesExcept("name", "email", "username");
        Assertions.assertThat(customer.getName()).isEqualTo(Data.NAME);
        Assertions.assertThat(customer.getEmail()).isEqualTo(Data.EMAIL);
        Assertions.assertThat(customer.getUsername()).isEqualTo(Data.USERNAME);
    }

    @Test
    void shouldReturnNullWhenCreateCustomerRequestToUserRequestIsNull() {
        Assertions.assertThat(mapper.toUserRequest(null)).isNull();
    }

    @Test
    void shouldMapToUserRequestWhenCreateCustomerRequestIsValid() {
        final var userRequest = mapper.toUserRequest(Data.CREATE_CUSTOMER_REQUEST);
        Assertions.assertThat(userRequest).isNotNull();
        Assertions.assertThat(userRequest.name()).isEqualTo(Data.NAME);
        Assertions.assertThat(userRequest.email()).isEqualTo(Data.EMAIL);
        Assertions.assertThat(userRequest.username()).isEqualTo(Data.USERNAME);
        Assertions.assertThat(userRequest.password()).isEqualTo(Data.PASSWORD);
    }
}
