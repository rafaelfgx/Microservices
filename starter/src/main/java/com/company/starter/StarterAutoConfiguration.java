package com.company.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableResilientMethods
@EnableScheduling
@ComponentScan(basePackageClasses = StarterAutoConfiguration.class)
@ConfigurationPropertiesScan(basePackageClasses = StarterAutoConfiguration.class)
@AutoConfigurationPackage(basePackageClasses = StarterAutoConfiguration.class)
@AutoConfiguration(before = DataMongoRepositoriesAutoConfiguration.class)
public class StarterAutoConfiguration {
}
