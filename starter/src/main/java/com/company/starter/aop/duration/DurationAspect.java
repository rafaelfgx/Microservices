package com.company.starter.aop.duration;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
public class DurationAspect {
    @Around("@annotation(duration)")
    public Object capture(final ProceedingJoinPoint joinPoint, final Duration duration) throws Throwable {
        final var start = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            final var elapsed = System.nanoTime() - start;
            final var signature = joinPoint.getSignature();
            final var className = signature.getDeclaringType().getSimpleName();
            final var methodName = signature.getName();
            final var time = TimeUnit.NANOSECONDS.toMillis(elapsed);
            log.atInfo().addKeyValue("class", className).addKeyValue("method", methodName).addKeyValue("time", time).log("[Duration]");
        }
    }
}
