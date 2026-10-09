package com.company.productservice.product.handlers;

import com.company.productservice.product.ProductRepository;
import com.company.productservice.product.requests.DeleteProductRequest;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DeleteProductHandler implements RequestResponseHandler<DeleteProductRequest, ResponseEntity<Void>> {
    private final ProductRepository productRepository;

    @Override
    public ResponseEntity<Void> handle(final DeleteProductRequest request) {
        productRepository.deleteById(request.id());
        return ResponseEntity.noContent().build();
    }
}
