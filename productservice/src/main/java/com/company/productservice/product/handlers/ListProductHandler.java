package com.company.productservice.product.handlers;

import com.company.productservice.product.ProductRepository;
import com.company.productservice.product.requests.ListProductRequest;
import com.company.productservice.product.responses.ProductResponse;
import com.company.starter.mediator.RequestResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ListProductHandler implements RequestResponseHandler<ListProductRequest, ResponseEntity<Page<ProductResponse>>> {
    private final ProductRepository productRepository;

    @Override
    public ResponseEntity<Page<ProductResponse>> handle(final ListProductRequest request) {
        return ResponseEntity.of(Optional.of(productRepository.findBy(request)).filter(Page::hasContent));
    }
}
