package com.company.configurationservice.configuration;

import com.company.configurationservice.configuration.requests.CreateConfigurationRequest;
import com.company.configurationservice.configuration.requests.UpdateConfigurationRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConfigurationMapper {
    Configuration toConfiguration(final CreateConfigurationRequest source);

    Configuration toConfiguration(final UpdateConfigurationRequest source);
}
