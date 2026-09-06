package com.company.productservice.product;

import com.company.productservice.product.domains.Product;
import com.company.productservice.product.requests.CreateProductRequest;
import com.company.productservice.product.requests.ListProductRequest;
import com.company.productservice.product.requests.UpdateProductRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Product toProduct(final CreateProductRequest request);

    Product toProduct(final UpdateProductRequest request);

    @BeanMapping(builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
    Product toProduct(final ListProductRequest request);
}
