package com.company.customerservice.customer;

import com.company.customerservice.customer.domains.Customer;
import com.company.customerservice.customer.requests.ListCustomerRequest;
import com.company.customerservice.customer.responses.CustomerResponse;
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
public interface CustomerRepository extends MongoRepository<Customer, UUID> {
    ExampleMatcher matcher = matching().withIgnoreNullValues().withStringMatcher(CONTAINING).withIgnoreCase();
    CustomerMapper mapper = Mappers.getMapper(CustomerMapper.class);

    boolean existsByEmailOrUsername(final String email, final String username);

    <T> Optional<T> findById(final UUID id, final Class<T> type);

    default Page<CustomerResponse> findBy(final ListCustomerRequest request) {
        return findBy(Example.of(mapper.toCustomer(request), matcher), query -> query.as(CustomerResponse.class).page(request.pageable()));
    }
}
