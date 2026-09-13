package com.company.authservice.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private String url;
    private String realm;
    private String clientId;
    private String clientSecret;
}
