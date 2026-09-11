package com.company.productservice.product;

import com.company.productservice.product.responses.ProductResponse;
import com.company.productservice.shared.Data;
import com.company.productservice.shared.MongoTestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@Import(MongoTestConfiguration.class)
@DataMongoTest
class ProductRepositoryTest {
    @Autowired
    MongoTemplate mongo;

    @Autowired
    ProductRepository repository;

    @BeforeEach
    void beforeEach() {
        mongo.getDb().drop();
    }

    @Test
    void shouldReturnEmptyListWhenNoProductsExist() {
        final var products = repository.findBy(Data.LIST_PRODUCT_REQUEST);
        Assertions.assertThat(products).isEmpty();
    }

    @Test
    void shouldReturnNonEmptyListWhenProductsExist() {
        repository.save(Data.PRODUCT);
        final var products = repository.findBy(Data.LIST_PRODUCT_REQUEST);
        Assertions.assertThat(products.get().toList().getFirst()).isEqualTo(Data.PRODUCT_RESPONSE);
    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExist() {
        final var product = repository.findById(Data.PRODUCT.getId(), ProductResponse.class);
        Assertions.assertThat(product).isEmpty();
    }

    @Test
    void shouldReturnNonEmptyWhenProductExists() {
        repository.save(Data.PRODUCT);
        final var product = repository.findById(Data.PRODUCT.getId(), ProductResponse.class);
        Assertions.assertThat(product).isPresent();
        Assertions.assertThat(product.get()).isEqualTo(Data.PRODUCT_RESPONSE);
    }
}
