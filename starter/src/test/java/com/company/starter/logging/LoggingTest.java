package com.company.starter.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.slf4j.LoggerFactory;

@CustomLog
@ResourceLock("com.company.starter.logging.LoggingTest")
class LoggingTest {
    @Test
    void shouldCreateLoggerWhenTypeIsProvided() {
        Assertions.assertInstanceOf(Slf4jApplicationLogger.class, log);
        Assertions.assertInstanceOf(Slf4jApplicationLogger.class, ApplicationLoggerFactory.getLogger(LoggingTest.class));
    }

    @Test
    void shouldRejectLoggerWhenTypeIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> new Slf4jApplicationLogger(null));
    }

    @Test
    void shouldRejectEventWhenMarkerIsNull() {
        createAppender();
        Assertions.assertThrows(IllegalArgumentException.class, () -> log.at().info().marker(null).message("Message").log());
    }

    @Test
    void shouldLogEventWhenMessageIsNull() {
        final var appender = createAppender();
        log.at().info().marker("Marker").message(null).log();
        Assertions.assertEquals(1, appender.list.size());
        Assertions.assertNull(appender.list.getFirst().getMessage());
    }

    @Test
    void shouldLogEventWhenAttributeKeyIsNull() {
        final var appender = createAppender();
        log.at().info().marker("Marker").message("Message").attribute(null, "value").log();
        Assertions.assertEquals(1, appender.list.size());
        Assertions.assertNull(appender.list.getFirst().getKeyValuePairs().getFirst().key);
        Assertions.assertEquals("value", appender.list.getFirst().getKeyValuePairs().getFirst().value);
    }

    @Test
    void shouldLogEventsWithMarkerAndAttributesWhenLevelIsEnabled() {
        final var appender = createAppender();
        log.at().info().marker("Marker").message("Message").attribute("first", 1).attribute("second", null).log();
        log.at().warn().marker("Marker").message("Message").attribute("first", 1).attribute("second", null).log();
        log.at().error().marker("Marker").message("Message").attribute("first", 1).attribute("second", null).log();
        Assertions.assertEquals(3, appender.list.size());
        Assertions.assertEquals(Level.INFO, appender.list.get(0).getLevel());
        Assertions.assertEquals(Level.WARN, appender.list.get(1).getLevel());
        Assertions.assertEquals(Level.ERROR, appender.list.get(2).getLevel());

        for (final var event : appender.list) {
            Assertions.assertEquals("Message", event.getFormattedMessage());
            Assertions.assertEquals("Marker", event.getMarkerList().getFirst().getName());
            Assertions.assertEquals(2, event.getKeyValuePairs().size());
            Assertions.assertEquals("first", event.getKeyValuePairs().getFirst().key);
            Assertions.assertEquals(1, event.getKeyValuePairs().getFirst().value);
            Assertions.assertEquals("second", event.getKeyValuePairs().getLast().key);
            Assertions.assertNull(event.getKeyValuePairs().getLast().value);
            Assertions.assertNull(event.getThrowableProxy());
        }

    }

    @Test
    void shouldLogEventsWhenOptionalStepsAreOrderedDifferently() {
        final var appender = createAppender();
        final var exception = new IllegalStateException("failure");
        log.at().info().marker("Marker").message("Message").attribute("first", 1).attribute("second", 2).enabled(true).log();
        log.at().warn().marker("Marker").message("Message").exception(exception).attribute("first", 1).enabled(true).log();
        log.at().error().marker("Marker").message("Message").exception(exception).attribute("first", 1).attribute("second", 2).enabled(true).log();
        Assertions.assertEquals(3, appender.list.size());
        Assertions.assertEquals(2, appender.list.get(0).getKeyValuePairs().size());
        Assertions.assertNotNull(appender.list.get(1).getThrowableProxy());
        Assertions.assertEquals(2, appender.list.get(2).getKeyValuePairs().size());
        Assertions.assertEquals(exception.getMessage(), appender.list.get(2).getThrowableProxy().getMessage());
    }

    @Test
    void shouldLogEventWithMarkerWhenMarkerIsEmpty() {
        final var appender = createAppender();
        log.at().info().marker("").message("Message").log();
        Assertions.assertEquals(1, appender.list.size());
        Assertions.assertEquals("", appender.list.getFirst().getMarkerList().getFirst().getName());
    }

    @Test
    void shouldSkipEventsWhenDisabled() {
        final var appender = createAppender();
        log.at().info().marker("Marker").message("Message").attribute("key", "value").enabled(false).log();
        log.at().warn().marker("Marker").message("Message").enabled(false).log();
        log.at().error().marker("Marker").message("Message").enabled(false).log();
        Assertions.assertTrue(appender.list.isEmpty());
    }

    @Test
    void shouldSkipEventsWhenLevelIsOff() {
        final var appender = createAppender();
        ((Logger) LoggerFactory.getLogger(LoggingTest.class)).setLevel(Level.OFF);
        log.at().info().marker("Marker").message("Message").log();
        log.at().warn().marker("Marker").message("Message").log();
        log.at().error().marker("Marker").message("Message").log();
        Assertions.assertTrue(appender.list.isEmpty());
    }

    private ListAppender<ILoggingEvent> createAppender() {
        final var logger = (Logger) LoggerFactory.getLogger(LoggingTest.class);
        logger.setLevel(Level.INFO);
        final var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }
}
