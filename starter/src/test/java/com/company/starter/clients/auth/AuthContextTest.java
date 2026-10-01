package com.company.starter.clients.auth;

import com.company.starter.configurations.RestClientConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.http.client.autoconfigure.HttpClientAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.service.HttpServiceClientPropertiesAutoConfiguration;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.service.HttpServiceClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.support.OAuth2RestClientHttpServiceGroupConfigurer;

class AuthContextTest {
    final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            JacksonAutoConfiguration.class,
            HttpMessageConvertersAutoConfiguration.class,
            HttpClientAutoConfiguration.class,
            RestClientAutoConfiguration.class,
            HttpServiceClientPropertiesAutoConfiguration.class,
            HttpServiceClientAutoConfiguration.class
        ))
        .withBean(OAuth2RestClientHttpServiceGroupConfigurer.class, () -> OAuth2RestClientHttpServiceGroupConfigurer.from(Mockito.mock(OAuth2AuthorizedClientManager.class)))
        .withUserConfiguration(RestClientConfiguration.class, AuthAmbassador.class);

    @Test
    void shouldRegisterAuthBeansWhenConfigured() {
        runner
            .withPropertyValues("spring.http.serviceclient.auth.base-url=http://localhost:8010")
            .run(context -> {
                Assertions.assertNotNull(context.getBean(AuthClient.class));
                Assertions.assertNotNull(context.getBean(AuthAmbassador.class));
            });
    }

    @Test
    void shouldNotRegisterAuthBeansWhenNotConfigured() {
        runner.run(context -> {
            Assertions.assertEquals(0, context.getBeanNamesForType(AuthClient.class).length);
            Assertions.assertEquals(0, context.getBeanNamesForType(AuthAmbassador.class).length);
        });
    }
}
