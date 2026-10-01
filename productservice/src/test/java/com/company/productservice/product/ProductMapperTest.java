package com.company.productservice.product;

import com.company.productservice.product.requests.CreateProductRequest;
import com.company.productservice.product.requests.ListProductRequest;
import com.company.productservice.product.requests.UpdateProductRequest;
import com.company.productservice.shared.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductMapperTest {
    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void shouldReturnNullWhenCreateProductRequestIsNull() {
        Assertions.assertThat(mapper.toProduct((CreateProductRequest) null)).isNull();
    }

    @Test
    void shouldMapToProductWhenCreateProductRequestIsValid() {
        final var product = mapper.toProduct(Data.CREATE_PRODUCT_REQUEST);
        Assertions.assertThat(product).isNotNull();
        Assertions.assertThat(product.getId()).isNotNull();
        Assertions.assertThat(product.getName()).isEqualTo(Data.CREATE_PRODUCT_REQUEST.name());
        Assertions.assertThat(product.getDescription()).isEqualTo(Data.CREATE_PRODUCT_REQUEST.description());
    }

    @Test
    void shouldReturnNullWhenUpdateProductRequestIsNull() {
        Assertions.assertThat(mapper.toProduct((UpdateProductRequest) null)).isNull();
    }

    @Test
    void shouldMapToProductWhenUpdateProductRequestIsValid() {
        final var product = mapper.toProduct(Data.UPDATE_PRODUCT_REQUEST);
        Assertions.assertThat(product).isNotNull();
        Assertions.assertThat(product.getId()).isEqualTo(Data.UPDATE_PRODUCT_REQUEST.id());
        Assertions.assertThat(product.getName()).isEqualTo(Data.UPDATE_PRODUCT_REQUEST.name());
        Assertions.assertThat(product.getDescription()).isEqualTo(Data.UPDATE_PRODUCT_REQUEST.description());
    }

    @Test
    void shouldReturnNullWhenListProductRequestIsNull() {
        Assertions.assertThat(mapper.toProduct((ListProductRequest) null)).isNull();
    }

    @Test
    void shouldMapToProductWhenListProductRequestIsValid() {
        final var product = mapper.toProduct(Data.LIST_PRODUCT_REQUEST);
        Assertions.assertThat(product).isNotNull();
        Assertions.assertThat(product).hasAllNullFieldsOrPropertiesExcept("name");
        Assertions.assertThat(product.getName()).isEqualTo(Data.LIST_PRODUCT_REQUEST.name());
    }
}
