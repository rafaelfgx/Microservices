package com.company.customerservice.shared;

import com.company.customerservice.customer.domains.Customer;
import com.company.customerservice.customer.requests.CreateCustomerRequest;
import com.company.customerservice.customer.requests.ListCustomerRequest;
import com.company.customerservice.customer.requests.UpdateCustomerRequest;
import com.company.customerservice.customer.responses.CustomerResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Data {
    public static final String NIL_UUID = "00000000-0000-0000-0000-000000000000";
    public static final String ID = "11111111-1111-1111-1111-111111111111";
    public static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final String NAME = "Name";
    public static final String NAME_UPDATED = "Name Updated";
    public static final String EMAIL = "email@mail.com";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "P@$$W0rd";

    public static final Customer CUSTOMER = new Customer(UUID.fromString(ID), NAME, EMAIL, USERNAME, USER_ID);
    public static final CustomerResponse CUSTOMER_RESPONSE = new CustomerResponse(UUID.fromString(ID), NAME, EMAIL, USERNAME, USER_ID);
    public static final CreateCustomerRequest CREATE_CUSTOMER_REQUEST = new CreateCustomerRequest(NAME, EMAIL, USERNAME, PASSWORD);
    public static final UpdateCustomerRequest UPDATE_CUSTOMER_REQUEST = new UpdateCustomerRequest(UUID.fromString(ID), NAME_UPDATED);
    public static final ListCustomerRequest LIST_CUSTOMER_REQUEST = new ListCustomerRequest(Pageable.unpaged(), NAME, EMAIL, USERNAME);
}
