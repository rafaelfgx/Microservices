package com.company.authservice.keycloak;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@ImportHttpServices(group = "keycloak", types = {KeycloakAdminClient.class, KeycloakTokenClient.class})
@Configuration(proxyBeanMethods = false)
public class KeycloakConfiguration {
}
