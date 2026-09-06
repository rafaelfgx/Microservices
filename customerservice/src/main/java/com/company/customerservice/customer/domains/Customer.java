package com.company.customerservice.customer.domains;

import com.company.starter.validation.username.Username;
import jakarta.validation.constraints.Email;
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
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Getter
@Setter(AccessLevel.PRIVATE)
@Document("customers")
public class Customer {
    @Id
    @NotNull
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @NotBlank
    private String name;

    @Indexed(unique = true)
    @Email
    @NotBlank
    private String email;

    @Indexed(unique = true)
    @Username
    @NotBlank
    private String username;

    @Indexed(unique = true)
    @NotNull
    private UUID userId;
}
