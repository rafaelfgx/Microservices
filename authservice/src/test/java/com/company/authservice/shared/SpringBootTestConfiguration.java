package com.company.authservice.shared;

import com.company.starter.exception.GlobalExceptionHandler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@Import({
    GlobalExceptionHandler.class,
    KeycloakTestConfiguration.class,
    KafkaTestConfiguration.class,
    MongoTestConfiguration.class,
    RedisTestConfiguration.class
})
public class SpringBootTestConfiguration {
}
