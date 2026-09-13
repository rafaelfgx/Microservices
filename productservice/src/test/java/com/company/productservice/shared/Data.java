package com.company.productservice.shared;

import com.company.productservice.product.domains.Product;
import com.company.productservice.product.requests.CreateProductRequest;
import com.company.productservice.product.requests.ListProductRequest;
import com.company.productservice.product.requests.UpdateProductRequest;
import com.company.productservice.product.responses.ProductResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Data {
    public static final String NIL_UUID = "00000000-0000-0000-0000-000000000000";
    public static final String ID = "11111111-1111-1111-1111-111111111111";
    public static final String NAME = "Name";
    public static final String DESCRIPTION = "Description";

    public static final Product PRODUCT = new Product(UUID.fromString(ID), NAME, DESCRIPTION);
    public static final ProductResponse PRODUCT_RESPONSE = new ProductResponse(UUID.fromString(ID), NAME, DESCRIPTION);
    public static final CreateProductRequest CREATE_PRODUCT_REQUEST = new CreateProductRequest(NAME, DESCRIPTION);
    public static final UpdateProductRequest UPDATE_PRODUCT_REQUEST = new UpdateProductRequest(UUID.fromString(ID), NAME, DESCRIPTION);
    public static final ListProductRequest LIST_PRODUCT_REQUEST = new ListProductRequest(Pageable.unpaged(), NAME);
}
