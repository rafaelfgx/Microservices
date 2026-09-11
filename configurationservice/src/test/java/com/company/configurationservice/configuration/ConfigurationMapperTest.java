package com.company.configurationservice.configuration;

import com.company.configurationservice.configuration.requests.CreateConfigurationRequest;
import com.company.configurationservice.configuration.requests.UpdateConfigurationRequest;
import com.company.configurationservice.shared.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ConfigurationMapperTest {
    private final ConfigurationMapper mapper = Mappers.getMapper(ConfigurationMapper.class);

    @Test
    void shouldReturnNullWhenCreateConfigurationRequestIsNull() {
        Assertions.assertThat(mapper.toConfiguration((CreateConfigurationRequest) null)).isNull();
    }

    @Test
    void shouldMapToConfigurationWhenCreateConfigurationRequestIsValid() {
        final var configuration = mapper.toConfiguration(Data.CREATE_CONFIGURATION_REQUEST);
        Assertions.assertThat(configuration).isNotNull();
        Assertions.assertThat(configuration.getId()).isEqualTo(Data.CREATE_CONFIGURATION_REQUEST.id());
        Assertions.assertThat(configuration.getValue()).isEqualTo(Data.CREATE_CONFIGURATION_REQUEST.value());
        Assertions.assertThat(configuration.getDescription()).isEqualTo(Data.CREATE_CONFIGURATION_REQUEST.description());
        Assertions.assertThat(configuration.getGroup()).isEqualTo(Data.CREATE_CONFIGURATION_REQUEST.group());
    }

    @Test
    void shouldReturnNullWhenUpdateConfigurationRequestIsNull() {
        Assertions.assertThat(mapper.toConfiguration((UpdateConfigurationRequest) null)).isNull();
    }

    @Test
    void shouldMapToConfigurationWhenUpdateConfigurationRequestIsValid() {
        final var configuration = mapper.toConfiguration(Data.UPDATE_CONFIGURATION_REQUEST);
        Assertions.assertThat(configuration).isNotNull();
        Assertions.assertThat(configuration.getId()).isEqualTo(Data.UPDATE_CONFIGURATION_REQUEST.id());
        Assertions.assertThat(configuration.getValue()).isEqualTo(Data.UPDATE_CONFIGURATION_REQUEST.value());
        Assertions.assertThat(configuration.getDescription()).isEqualTo(Data.UPDATE_CONFIGURATION_REQUEST.description());
        Assertions.assertThat(configuration.getGroup()).isEqualTo(Data.UPDATE_CONFIGURATION_REQUEST.group());
    }
}
