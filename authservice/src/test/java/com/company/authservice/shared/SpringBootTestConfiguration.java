package com.company.authservice.shared;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@Import({
    KeycloakTestConfiguration.class,
    KafkaTestConfiguration.class,
    MongoTestConfiguration.class,
    RedisTestConfiguration.class
})
public class SpringBootTestConfiguration {
}
