package com.company.starter.clients;

import java.net.URI;
import java.time.Duration;

public interface ClientProperties {
    URI url();

    Duration connectTimeout();

    Duration readTimeout();

    default String registrationId() {
        return "default";
    }
}
