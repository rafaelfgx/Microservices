package com.company.starter.configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.util.ErrorHandler;

class ConfigurationStreamConfigurationTest {
    private final Lifecycle container = Mockito.mock();
    private final ErrorHandler errorHandler = ConfigurationStreamConfiguration.errorHandler(container);
    private final IllegalStateException exception = new IllegalStateException("failure");

    @Test
    void shouldLogErrorWhenContainerIsRunning() {
        final var appender = createAppender();
        Mockito.when(container.isRunning()).thenReturn(true);
        errorHandler.handleError(exception);
        Assertions.assertEquals(1, appender.list.size());
        Assertions.assertEquals(Level.ERROR, appender.list.getFirst().getLevel());
        Assertions.assertEquals(exception.getMessage(), appender.list.getFirst().getThrowableProxy().getMessage());
    }

    @Test
    void shouldSkipErrorWhenContainerIsStopped() {
        final var appender = createAppender();
        Mockito.when(container.isRunning()).thenReturn(false);
        errorHandler.handleError(exception);
        Assertions.assertTrue(appender.list.isEmpty());
    }

    private ListAppender<ILoggingEvent> createAppender() {
        final var logger = (Logger) LoggerFactory.getLogger(ConfigurationStreamConfiguration.class);
        final var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }
}
