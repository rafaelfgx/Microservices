package com.company.starter.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Slf4jApplicationLogger implements ApplicationLogger {
    private final Logger logger;

    public Slf4jApplicationLogger(final Class<?> type) {
        this.logger = LoggerFactory.getLogger(type);
    }

    @Override
    public LevelStep at() {
        return new Builder();
    }

    private final class Builder implements LevelStep, MarkerStep, MessageStep, OptionalStep, AfterExceptionStep, LogStep {
        private Level level;
        private String marker;
        private String message;
        private Throwable exception;
        private boolean enabled = true;

        private final Map<String, Object> attributes = new LinkedHashMap<>();

        @Override
        public MarkerStep info() {
            level = Level.INFO;
            return this;
        }

        @Override
        public MarkerStep warn() {
            level = Level.WARN;
            return this;
        }

        @Override
        public MarkerStep error() {
            level = Level.ERROR;
            return this;
        }

        @Override
        public MessageStep marker(final String marker) {
            this.marker = marker;
            return this;
        }

        @Override
        public OptionalStep message(final String message) {
            this.message = message;
            return this;
        }

        @Override
        public Builder exception(final Throwable exception) {
            this.exception = exception;
            return this;
        }

        @Override
        public Builder enabled(final boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        @Override
        public Builder attribute(final String key, final Object value) {
            attributes.put(key, value);
            return this;
        }

        @Override
        public void log() {
            if (!enabled || !logger.isEnabledForLevel(level)) return;
            final var event = logger.atLevel(level);
            event.addMarker(MarkerFactory.getMarker(marker));
            event.setMessage(message);
            event.setCause(exception);
            attributes.forEach(event::addKeyValue);
            event.log();
        }
    }
}
