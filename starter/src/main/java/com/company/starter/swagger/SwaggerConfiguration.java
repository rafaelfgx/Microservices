package com.company.starter.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {
    @Bean
    public OpenAPI openapi() {
        final var scheme = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT");
        final var components = new Components().addSecuritySchemes("JWT", scheme);
        final var requirement = new SecurityRequirement().addList("JWT");
        return new OpenAPI().components(components).addSecurityItem(requirement);
    }
}
