package com.company.customerservice.customer;

import com.company.customerservice.customer.domains.Customer;
import com.company.customerservice.customer.requests.CreateCustomerRequest;
import com.company.customerservice.customer.requests.ListCustomerRequest;
import com.company.customerservice.customer.requests.UpdateCustomerRequest;
import com.company.starter.clients.auth.dtos.UserRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(source = "userId", target = "userId")
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Customer toCustomer(final CreateCustomerRequest request, final UUID userId);

    @Mapping(source = "customer.id", target = "id")
    @Mapping(source = "request.name", target = "name")
    Customer toCustomer(final Customer customer, final UpdateCustomerRequest request);

    @BeanMapping(builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Customer toCustomer(final ListCustomerRequest request);

    UserRequest toUserRequest(final CreateCustomerRequest request);
}
