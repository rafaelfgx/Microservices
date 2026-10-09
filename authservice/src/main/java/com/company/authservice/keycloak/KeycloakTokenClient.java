package com.company.authservice.keycloak;

import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/realms/${spring.http.serviceclient.keycloak.realm}/protocol/openid-connect/token")
public interface KeycloakTokenClient {
    @PostExchange(contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KeycloakToken token(@RequestParam MultiValueMap<String, String> form);
}
