package com.company.productservice.product.handlers;

import com.company.productservice.product.ProductRepository;
import com.company.productservice.product.requests.GetProductRequest;
import com.company.productservice.product.responses.ProductResponse;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetProductHandler implements RequestResponseHandler<GetProductRequest, ResponseEntity<ProductResponse>> {
    private final ProductRepository productRepository;

    @Override
    public ResponseEntity<ProductResponse> handle(final GetProductRequest request) {
        return ResponseEntity.of(productRepository.findById(request.id(), ProductResponse.class));
    }
}
