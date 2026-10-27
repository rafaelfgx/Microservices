package com.company.productservice.product.domains;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Getter
@Setter(AccessLevel.PRIVATE)
@Document("products")
public class Product {
    @Id
    @NotNull
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @TextIndexed
    @NotBlank
    private String name;

    @TextIndexed
    @NotBlank
    private String description;
}
