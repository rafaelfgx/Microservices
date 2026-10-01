package com.company.starter;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableResilientMethods
@EnableScheduling
@AutoConfigureBefore(DataMongoRepositoriesAutoConfiguration.class)
@AutoConfigurationPackage(basePackages = "com.company.starter")
@ComponentScan("com.company.starter")
@ConfigurationPropertiesScan("com.company.starter")
@org.springframework.boot.autoconfigure.AutoConfiguration
public class AutoConfiguration {
}
