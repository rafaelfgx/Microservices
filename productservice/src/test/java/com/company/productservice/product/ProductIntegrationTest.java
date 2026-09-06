package com.company.productservice.product;

import com.company.productservice.product.responses.ProductResponse;
import com.company.productservice.shared.Data;
import com.company.productservice.shared.SpringBootTestConfiguration;
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

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureMockMvc(addFilters = false)
@Import(SpringBootTestConfiguration.class)
@SpringBootTest
class ProductIntegrationTest {
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
    void shouldReturnNotFoundWhenProductDoesNotExist(final String uri) {
        mongo.save(Data.PRODUCT);
        final var result = mvc.get().uri("/products" + uri).exchange();
        Assertions.assertThat(result).hasStatus(NOT_FOUND);
    }

    @ParameterizedTest
    @ValueSource(strings = {"page=0", "size=99999", "sort=id", "sort=name", "direction=ASC", "direction=DESC", "name=" + Data.NAME})
    void shouldReturnOkWhenProductsExist(final String uri) {
        mongo.save(Data.PRODUCT);
        final var result = mvc.get().uri("/products?" + uri).exchange();
        Assertions.assertThat(result).hasStatus(OK);
    }

    @Test
    void shouldReturnOkWhenProductExists() {
        mongo.save(Data.PRODUCT);
        final var result = mvc.get().uri("/products/{id}", Data.ID).exchange();
        Assertions.assertThat(result).hasStatus(OK).bodyJson().convertTo(ProductResponse.class).isEqualTo(Data.PRODUCT_RESPONSE);
    }

    @Test
    void shouldReturnCreatedWhenCreatingProduct() {
        final var result = mvc.post().uri("/products").contentType(APPLICATION_JSON).content(json.writeValueAsString(Data.CREATE_PRODUCT_REQUEST)).exchange();
        Assertions.assertThat(result).hasStatus(CREATED).bodyText().isNotBlank();
    }

    @ParameterizedTest
    @ValueSource(strings = {Data.NIL_UUID, Data.ID})
    void shouldReturnNoContentWhenUpdatingProduct(final String id) {
        mongo.save(Data.PRODUCT);
        final var result = mvc.put().uri("/products/{id}", id).contentType(APPLICATION_JSON).content(json.writeValueAsString(Data.UPDATE_PRODUCT_REQUEST)).exchange();
        Assertions.assertThat(result).hasStatus(NO_CONTENT);
    }

    @ParameterizedTest
    @ValueSource(strings = {Data.NIL_UUID, Data.ID})
    void shouldReturnNoContentWhenDeletingProduct(final String id) {
        mongo.save(Data.PRODUCT);
        final var result = mvc.delete().uri("/products/{id}", id).exchange();
        Assertions.assertThat(result).hasStatus(NO_CONTENT);
    }
}
