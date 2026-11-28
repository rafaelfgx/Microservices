package com.company.customerservice.customer;

import com.company.customerservice.customer.domains.Customer;
import com.company.customerservice.customer.requests.ListCustomerRequest;
import com.company.customerservice.customer.responses.CustomerResponse;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, UUID> {
    ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase();
    CustomerMapper mapper = Mappers.getMapper(CustomerMapper.class);

    @Aggregation(pipeline = {"{ '$match': { 'userId': ?0 } }", "{ '$project': { '_id': 1 } }"})
    Optional<UUID> findCustomerIdByUserId(final UUID userId);

    <T> Optional<T> findById(final UUID id, final Class<T> type);

    default Page<CustomerResponse> findBy(final ListCustomerRequest request) {
        return findBy(Example.of(mapper.toCustomer(request), matcher), query -> query.as(CustomerResponse.class).page(request.pageable()));
    }
}
