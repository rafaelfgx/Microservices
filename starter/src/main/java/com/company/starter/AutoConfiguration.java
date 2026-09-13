package com.company.starter;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.company.starter")
@ConfigurationPropertiesScan("com.company.starter")
@org.springframework.boot.autoconfigure.AutoConfiguration
public class AutoConfiguration {
}
