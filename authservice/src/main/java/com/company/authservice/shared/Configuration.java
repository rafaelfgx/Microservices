package com.company.authservice.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

@org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
public class Configuration {
    @Bean
    public Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> authCustomizer() {
        return registry -> {
            registry.requestMatchers(HttpMethod.POST, "/auth").permitAll();
            registry.requestMatchers(HttpMethod.POST, "/users").hasRole("users:manage");
            registry.requestMatchers(HttpMethod.PATCH, "/users/{id}/verify").hasRole("users:manage");
            registry.requestMatchers(HttpMethod.DELETE, "/users/{id}").hasRole("users:manage");
        };
    }
}
