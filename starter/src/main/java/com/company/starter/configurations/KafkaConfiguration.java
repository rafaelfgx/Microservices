package com.company.starter.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.JacksonJsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
public class KafkaConfiguration {
    @Bean
    public RecordMessageConverter kafkaRecordMessageConverter(final JsonMapper json) {
        return new JacksonJsonMessageConverter(json);
    }
}
