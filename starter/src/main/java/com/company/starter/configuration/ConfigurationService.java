package com.company.starter.configuration;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ConfigurationService {
    private final ConversionService conversionService;
    private volatile Map<String, Configuration> configurations = Map.of();

    public List<Configuration> get() {
        return List.copyOf(configurations.values());
    }

    public Optional<Configuration> get(final String id) {
        return Optional.ofNullable(id).map(String::toLowerCase).map(configurations::get);
    }

    public <T> Optional<T> getValue(final String id, final Class<T> type) {
        try {
            return get(id).map(Configuration::value).map(String::valueOf).map(value -> conversionService.convert(value, type));
        } catch (final Exception ignored) {
            return Optional.empty();
        }
    }

    public Optional<String> getString(final String id) {
        return getValue(id, String.class);
    }

    public Optional<Boolean> getBoolean(final String id) {
        return getValue(id, Boolean.class);
    }

    public Optional<Integer> getInteger(final String id) {
        return getValue(id, Integer.class);
    }

    public Optional<Long> getLong(final String id) {
        return getValue(id, Long.class);
    }

    public Optional<BigDecimal> getDecimal(final String id) {
        return getValue(id, BigDecimal.class);
    }

    public Optional<LocalDate> getDate(final String id) {
        return getValue(id, LocalDate.class);
    }

    public Optional<LocalDateTime> getDateTime(final String id) {
        return getValue(id, LocalDateTime.class);
    }

    void setConfigurations(final @Nullable List<Configuration> configurations) {
        this.configurations = Objects.requireNonNullElse(configurations, List.<Configuration>of()).stream().collect(Collectors.toUnmodifiableMap(configuration -> configuration.id().toLowerCase(), Function.identity(), (_, last) -> last));
    }
}
