package com.company.starter.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

@Slf4j
class LoggingTest {
    @BeforeAll
    static void beforeAll() {
        final var context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.putProperty("spring.application.name", "Application");

        final var encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%n[%d{yyyy-MM-dd}] [%d{HH:mm:ss.SSS}] [%property{spring.application.name}] [%thread] %cyan([%logger] [%method] [%line] [%marker]) %highlight([%level]): %msg %magenta(%kvp %mdc) %ex%n%n");
        encoder.start();

        final var appender = new ConsoleAppender<ILoggingEvent>();
        appender.setContext(context);
        appender.setEncoder(encoder);
        appender.start();

        final var logger = context.getLogger("ROOT");
        logger.detachAndStopAllAppenders();
        logger.addAppender(appender);
    }

    @Test
    void shouldLog() {
        log
            .atInfo()
            .addMarker(MarkerFactory.getMarker("MARKER"))
            .addKeyValue("key", "value")
            .log("Message");

        log
            .atError()
            .addMarker(MarkerFactory.getMarker("MARKER"))
            .setCause(new Exception("Exception"))
            .addKeyValue("key", "value")
            .log("Message");
    }
}
