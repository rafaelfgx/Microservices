package com.company.starter.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

@Slf4j
class LoggingTest {
    @BeforeAll
    static void beforeAll() {
        final var context = (LoggerContext) LoggerFactory.getILoggerFactory();

        final var encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("[%d{yyyy-MM-dd}] [%d{HH:mm:ss.SSS}] [%thread] %cyan([%logger] [%method] [%line]) [%magenta(%kvp)] %highlight([%level]): %msg %ex{short} %n%n");
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
    void shouldLogWhenInfoLevelIsUsed() {
        log.atInfo().addKeyValue("id", "1").addKeyValue("name", "Name").log("Message");
    }

    @Test
    void shouldLogWhenErrorLevelIsUsed() {
        log.atError().setCause(new Exception("Exception")).addKeyValue("key", "value").log("Message");
    }
}
