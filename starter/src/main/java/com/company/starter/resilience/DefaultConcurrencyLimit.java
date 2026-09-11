package com.company.starter.resilience;

import org.springframework.core.annotation.AliasFor;
import org.springframework.resilience.annotation.ConcurrencyLimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ConcurrencyLimit(
    limitString = "${resilience.concurrency.limit}",
    policy = ConcurrencyLimit.ThrottlePolicy.BLOCK
)
public @interface DefaultConcurrencyLimit {
    @AliasFor(annotation = ConcurrencyLimit.class, attribute = "limitString")
    String limit() default "${resilience.concurrency.limit}";
}
