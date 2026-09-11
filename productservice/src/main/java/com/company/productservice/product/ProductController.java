package com.company.productservice.product;

import com.company.productservice.product.handlers.CreateProductHandler;
import com.company.productservice.product.handlers.DeleteProductHandler;
import com.company.productservice.product.handlers.GetProductHandler;
import com.company.productservice.product.handlers.ListProductHandler;
import com.company.productservice.product.handlers.UpdateProductHandler;
import com.company.productservice.product.requests.CreateProductRequest;
import com.company.productservice.product.requests.DeleteProductRequest;
import com.company.productservice.product.requests.GetProductRequest;
import com.company.productservice.product.requests.ListProductRequest;
import com.company.productservice.product.requests.UpdateProductRequest;
import com.company.productservice.product.responses.ProductResponse;
import com.company.starter.mediator.Mediator;
import com.company.starter.swagger.DefaultApiResponse;
import com.company.starter.swagger.GetApiResponse;
import com.company.starter.swagger.PostApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Products")
@RequestMapping("/products")
@RequiredArgsConstructor
@RestController
public class ProductController {
    private final Mediator mediator;

    @Operation(summary = "List")
    @GetApiResponse
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> list(@ParameterObject @Valid final ListProductRequest request, @ParameterObject @Valid final Pageable pageable) {
        return mediator.handle(ListProductHandler.class, request.withPageable(pageable));
    }

    @Operation(summary = "Get")
    @GetApiResponse
    @GetMapping("{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable final UUID id) {
        return mediator.handle(GetProductHandler.class, new GetProductRequest(id));
    }

    @Operation(summary = "Create")
    @PostApiResponse
    @PostMapping
    public ResponseEntity<UUID> create(@RequestBody @Valid final CreateProductRequest request) {
        return mediator.handle(CreateProductHandler.class, request);
    }

    @Operation(summary = "Update")
    @DefaultApiResponse
    @PutMapping("{id}")
    public ResponseEntity<Void> update(@PathVariable final UUID id, @RequestBody @Valid final UpdateProductRequest request) {
        return mediator.handle(UpdateProductHandler.class, request.withId(id));
    }

    @Operation(summary = "Delete")
    @DefaultApiResponse
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable final UUID id) {
        return mediator.handle(DeleteProductHandler.class, new DeleteProductRequest(id));
    }
}
