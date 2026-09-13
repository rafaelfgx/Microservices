package com.company.productservice.product.handlers;

import com.company.productservice.product.ProductMapper;
import com.company.productservice.product.ProductRepository;
import com.company.productservice.product.requests.CreateProductRequest;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class CreateProductHandler implements RequestResponseHandler<CreateProductRequest, ResponseEntity<UUID>> {
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;

    @Override
    public ResponseEntity<UUID> handle(final CreateProductRequest request) {
        final var product = productRepository.save(productMapper.toProduct(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(product.getId());
    }
}
