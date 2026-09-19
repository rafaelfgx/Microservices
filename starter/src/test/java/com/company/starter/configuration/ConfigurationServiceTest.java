package com.company.starter.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.boot.convert.ApplicationConversionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

class ConfigurationServiceTest {
    ConfigurationService configurationService = new ConfigurationService(new ApplicationConversionService());

    private void configure(final Configuration... configurations) {
        this.configurationService.setConfigurations(List.of(configurations));
    }

    @Test
    void shouldReturnEmptyOptionalWhenIdDoesNotExist() {
        Assertions.assertTrue(configurationService.get("id").isEmpty());
    }

    @Test
    void shouldReturnConfigurationWhenIdExists() {
        configure(new Configuration("id", "value", "description", "group"));
        Assertions.assertTrue(configurationService.get("id").isPresent());
    }

    @Test
    void shouldBeCaseInsensitiveWhenSearchingById() {
        configure(new Configuration("id", "value", "description", "group"));
        Assertions.assertTrue(configurationService.get("id").isPresent());
        Assertions.assertTrue(configurationService.get("Id").isPresent());
        Assertions.assertTrue(configurationService.get("iD").isPresent());
        Assertions.assertTrue(configurationService.get("ID").isPresent());
    }

    @Test
    void shouldReturnCorrectValueWhenGetValueIsCalled() {
        configure(new Configuration("id", "value", "description", "group"));
        Assertions.assertEquals(Optional.of("value"), configurationService.getString("id"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldHandleGracefullyWhenConfigurationsAreNullOrEmpty(final List<Configuration> configurations) {
        this.configurationService.setConfigurations(configurations);
        Assertions.assertTrue(this.configurationService.get("id").isEmpty());
        Assertions.assertEquals(Optional.empty(), this.configurationService.getString("id"));
    }

    @Test
    void shouldReflectNewValuesWhenConfigurationsAreUpdated() {
        configure(new Configuration("timeout", 1000, "Old", "System"));
        Assertions.assertEquals(Optional.of(1000), configurationService.getInteger("timeout"));
        configure(new Configuration("timeout", 5000, "New", "System"));
        Assertions.assertEquals(Optional.of(5000), configurationService.getInteger("timeout"));
    }

    @Test
    void shouldReturnFirstMatchWhenDuplicateIdsExistInList() {
        configure(
            new Configuration("duplicate", "first", "description", "group"),
            new Configuration("duplicate", "second", "description", "group")
        );

        Assertions.assertEquals(Optional.of("second"), configurationService.getString("duplicate"));
    }

    @Test
    void shouldConvertValueWhenTargetTypeIsString() {
        configure(
            new Configuration("1", "value", "description", "group"),
            new Configuration("2", 123, "description", "group"),
            new Configuration("3", true, "description", "group"),
            new Configuration("4", "", "description", "group"),
            new Configuration("5", null, "description", "group")
        );

        Assertions.assertEquals(Optional.of("value"), configurationService.getString("1"));
        Assertions.assertEquals(Optional.of("123"), configurationService.getString("2"));
        Assertions.assertEquals(Optional.of("true"), configurationService.getString("3"));
        Assertions.assertEquals(Optional.of(""), configurationService.getString("4"));
        Assertions.assertEquals(Optional.empty(), configurationService.getString("5"));
        Assertions.assertEquals(Optional.empty(), configurationService.getString("inexistent"));
    }

    @Test
    void shouldConvertValueWhenTargetTypeIsBoolean() {
        configure(
            new Configuration("1", false, "description", "group"),
            new Configuration("2", true, "description", "group"),
            new Configuration("3", "false", "description", "group"),
            new Configuration("4", "true", "description", "group"),
            new Configuration("5", 0, "description", "group"),
            new Configuration("6", 1, "description", "group"),
            new Configuration("7", "test", "description", "group"),
            new Configuration("8", null, "description", "group")
        );

        Assertions.assertEquals(Optional.of(false), configurationService.getBoolean("1"));
        Assertions.assertEquals(Optional.of(true), configurationService.getBoolean("2"));
        Assertions.assertEquals(Optional.of(false), configurationService.getBoolean("3"));
        Assertions.assertEquals(Optional.of(true), configurationService.getBoolean("4"));
        Assertions.assertEquals(Optional.of(false), configurationService.getBoolean("5"));
        Assertions.assertEquals(Optional.of(true), configurationService.getBoolean("6"));
        Assertions.assertEquals(Optional.empty(), configurationService.getBoolean("7"));
        Assertions.assertEquals(Optional.empty(), configurationService.getBoolean("8"));
        Assertions.assertEquals(Optional.empty(), configurationService.getBoolean("inexistent"));
    }

    @Test
    void shouldConvertValueWhenTargetTypeIsInteger() {
        configure(
            new Configuration("1", 50, "description", "group"),
            new Configuration("2", "100", "description", "group"),
            new Configuration("3", "not-a-number", "description", "group"),
            new Configuration("4", null, "description", "group")
        );

        Assertions.assertEquals(Optional.of(50), configurationService.getInteger("1"));
        Assertions.assertEquals(Optional.of(100), configurationService.getInteger("2"));
        Assertions.assertTrue(configurationService.getInteger("3").isEmpty());
        Assertions.assertTrue(configurationService.getInteger("4").isEmpty());
        Assertions.assertTrue(configurationService.getInteger("inexistent").isEmpty());
    }

    @Test
    void shouldConvertValueWhenTargetTypeIsLong() {
        configure(
            new Configuration("1", 500L, "description", "group"),
            new Configuration("2", "1000", "description", "group"),
            new Configuration("3", "abc", "description", "group"),
            new Configuration("4", null, "description", "group")
        );

        Assertions.assertEquals(Optional.of(500L), configurationService.getLong("1"));
        Assertions.assertEquals(Optional.of(1000L), configurationService.getLong("2"));
        Assertions.assertTrue(configurationService.getLong("3").isEmpty());
        Assertions.assertTrue(configurationService.getLong("4").isEmpty());
        Assertions.assertTrue(configurationService.getLong("inexistent").isEmpty());
    }

    @Test
    void shouldConvertValueWhenTargetTypeIsDecimal() {
        configure(
            new Configuration("1", new BigDecimal("12.34"), "description", "group"),
            new Configuration("2", "56.78", "description", "group"),
            new Configuration("3", "abc", "description", "group"),
            new Configuration("4", null, "description", "group")
        );

        Assertions.assertEquals(Optional.of(new BigDecimal("12.34")), configurationService.getDecimal("1"));
        Assertions.assertEquals(Optional.of(new BigDecimal("56.78")), configurationService.getDecimal("2"));
        Assertions.assertTrue(configurationService.getDecimal("3").isEmpty());
        Assertions.assertTrue(configurationService.getDecimal("4").isEmpty());
        Assertions.assertTrue(configurationService.getDecimal("inexistent").isEmpty());
    }

    @Test
    void shouldConvertValueWhenTargetTypeIsDate() {
        configure(
            new Configuration("1", "2026-12-31", "description", "group"),
            new Configuration("2", "invalid-date", "description", "group"),
            new Configuration("3", null, "description", "group")
        );

        Assertions.assertEquals(Optional.of(LocalDate.of(2026, 12, 31)), configurationService.getDate("1"));
        Assertions.assertTrue(configurationService.getDate("2").isEmpty());
        Assertions.assertTrue(configurationService.getDate("3").isEmpty());
        Assertions.assertTrue(configurationService.getDate("inexistent").isEmpty());
    }

    @Test
    void shouldConvertValueWhenTargetTypeIsDateTime() {
        configure(
            new Configuration("1", "2026-12-31T10:15:30", "description", "group"),
            new Configuration("2", "invalid-datetime", "description", "group"),
            new Configuration("3", null, "description", "group")
        );

        Assertions.assertEquals(Optional.of(LocalDateTime.of(2026, 12, 31, 10, 15, 30)), configurationService.getDateTime("1"));
        Assertions.assertTrue(configurationService.getDateTime("2").isEmpty());
        Assertions.assertTrue(configurationService.getDateTime("3").isEmpty());
        Assertions.assertTrue(configurationService.getDateTime("inexistent").isEmpty());
    }
}
