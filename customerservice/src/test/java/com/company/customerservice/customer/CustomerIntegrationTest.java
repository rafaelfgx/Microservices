package com.company.customerservice.customer;

import com.company.customerservice.customer.responses.CustomerResponse;
import com.company.customerservice.shared.Data;
import com.company.customerservice.shared.SpringBootTestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureMockMvc(addFilters = false)
@Import(SpringBootTestConfiguration.class)
@SpringBootTest
class CustomerIntegrationTest {
    @Autowired
    MockMvcTester mvc;

    @Autowired
    JsonMapper json;

    @Autowired
    MongoTemplate mongo;

    @BeforeEach
    void beforeEach() {
        mongo.getDb().drop();
    }

    @ParameterizedTest
    @ValueSource(strings = {"?name=inexistent", "/" + Data.NIL_UUID})
    void shouldReturnNotFoundWhenCustomerDoesNotExist(final String uri) {
        mongo.save(Data.CUSTOMER);
        final var result = mvc.get().uri("/customers" + uri).exchange();
        Assertions.assertThat(result).hasStatus(NOT_FOUND);
    }

    @ParameterizedTest
    @ValueSource(strings = {"page=0", "size=99999", "sort=id", "sort=name", "direction=ASC", "direction=DESC", "name=" + Data.NAME})
    void shouldReturnOkWhenCustomersExist(final String uri) {
        mongo.save(Data.CUSTOMER);
        final var result = mvc.get().uri("/customers?" + uri).exchange();
        Assertions.assertThat(result).hasStatus(OK);
    }

    @Test
    void shouldReturnOkWhenCustomerExists() {
        mongo.save(Data.CUSTOMER);
        final var result = mvc.get().uri("/customers/{id}", Data.ID).exchange();
        Assertions.assertThat(result).hasStatus(OK).bodyJson().convertTo(CustomerResponse.class).isEqualTo(Data.CUSTOMER_RESPONSE);
    }

    @Test
    void shouldReturnCreatedWhenCreatingCustomer() {
        final var result = mvc.post().uri("/customers").contentType(APPLICATION_JSON).content(json.writeValueAsString(Data.CREATE_CUSTOMER_REQUEST)).exchange();
        Assertions.assertThat(result).hasStatus(CREATED);
    }

    @Test
    void shouldReturnConflictWhenCustomerAlreadyExists() {
        mongo.save(Data.CUSTOMER);
        final var result = mvc.post().uri("/customers").contentType(APPLICATION_JSON).content(json.writeValueAsString(Data.CREATE_CUSTOMER_REQUEST)).exchange();
        Assertions.assertThat(result).hasStatus(CONFLICT);
    }

    @ParameterizedTest
    @ValueSource(strings = {Data.NIL_UUID, Data.ID})
    void shouldReturnNoContentWhenUpdatingCustomer(final String id) {
        mongo.save(Data.CUSTOMER);
        final var result = mvc.put().uri("/customers/{id}", id).contentType(APPLICATION_JSON).content(json.writeValueAsString(Data.UPDATE_CUSTOMER_REQUEST)).exchange();
        Assertions.assertThat(result).hasStatus(NO_CONTENT);
    }

    @ParameterizedTest
    @ValueSource(strings = {Data.NIL_UUID, Data.ID})
    void shouldReturnNoContentWhenDeletingCustomer(final String id) {
        mongo.save(Data.CUSTOMER);
        final var result = mvc.delete().uri("/customers/" + id).exchange();
        Assertions.assertThat(result).hasStatus(NO_CONTENT);
    }
}
