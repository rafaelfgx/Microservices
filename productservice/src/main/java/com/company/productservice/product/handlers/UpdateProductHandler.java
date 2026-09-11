package com.company.productservice.product.handlers;

import com.company.productservice.product.ProductMapper;
import com.company.productservice.product.ProductRepository;
import com.company.productservice.product.requests.UpdateProductRequest;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UpdateProductHandler implements RequestResponseHandler<UpdateProductRequest, ResponseEntity<Void>> {
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;

    @Override
    public ResponseEntity<Void> handle(final UpdateProductRequest request) {
        productRepository.save(productMapper.toProduct(request));
        return ResponseEntity.noContent().build();
    }
}
