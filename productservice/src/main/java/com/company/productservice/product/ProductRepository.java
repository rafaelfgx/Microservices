package com.company.productservice.product;

import com.company.productservice.product.domains.Product;
import com.company.productservice.product.requests.ListProductRequest;
import com.company.productservice.product.responses.ProductResponse;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.domain.ExampleMatcher.StringMatcher.CONTAINING;
import static org.springframework.data.domain.ExampleMatcher.matching;

@Repository
public interface ProductRepository extends MongoRepository<Product, UUID> {
    ExampleMatcher matcher = matching().withIgnoreNullValues().withStringMatcher(CONTAINING).withIgnoreCase();
    ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    <T> Optional<T> findById(final UUID id, final Class<T> type);

    default Page<ProductResponse> findBy(final ListProductRequest request) {
        return findBy(Example.of(mapper.toProduct(request), matcher), query -> query.as(ProductResponse.class).page(request.pageable()));
    }
}
