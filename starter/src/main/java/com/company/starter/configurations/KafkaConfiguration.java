package com.company.starter.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.support.converter.JacksonJsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class KafkaConfiguration implements KafkaListenerConfigurer {
    private final LocalValidatorFactoryBean validator;

    @Override
    public void configureKafkaListeners(final KafkaListenerEndpointRegistrar registrar) {
        registrar.setValidator(validator);
    }

    @Bean
    public RecordMessageConverter kafkaRecordMessageConverter(final JsonMapper json) {
        return new JacksonJsonMessageConverter(json);
    }
}
