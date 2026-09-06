package com.company.starter.clients.auth;

import com.company.starter.clients.ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

@ConfigurationProperties("clients.auth")
public record AuthProperties(URI url, Duration connectTimeout, Duration readTimeout) implements ClientProperties {
}
