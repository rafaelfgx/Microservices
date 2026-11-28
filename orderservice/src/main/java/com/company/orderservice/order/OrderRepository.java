package com.company.orderservice.order;

import com.company.orderservice.order.domains.Order;
import com.company.orderservice.order.requests.ListOrderRequest;
import com.company.orderservice.order.responses.OrderResponse;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends MongoRepository<Order, UUID> {
    ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase();
    OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    <T> Optional<T> findById(final UUID id, final Class<T> type);

    default Page<OrderResponse> findBy(final ListOrderRequest request) {
        return findBy(Example.of(mapper.toOrder(request), matcher), query -> query.as(OrderResponse.class).page(request.pageable()));
    }
}
