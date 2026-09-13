package com.company.starter.aop.duration;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DurationAspectTest {
    private final DurationAspect durationAspect = new DurationAspect();

    @Test
    void shouldCaptureDurationAndReturnProceedResult() throws Throwable {
        final var joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        final var signature = Mockito.mock(Signature.class);
        final var duration = Mockito.mock(Duration.class);
        final var expected = new Object();

        Mockito.when(joinPoint.proceed()).thenReturn(expected);
        Mockito.when(joinPoint.getSignature()).thenReturn(signature);
        Mockito.when(signature.getDeclaringType()).thenReturn(DurationAspectTest.class);
        Mockito.when(signature.getName()).thenReturn("captureMethod");

        final var result = durationAspect.capture(joinPoint, duration);

        Assertions.assertSame(expected, result);
        Mockito.verify(joinPoint, Mockito.times(1)).proceed();
        Mockito.verify(joinPoint, Mockito.times(1)).getSignature();
        Mockito.verify(signature, Mockito.times(1)).getDeclaringType();
        Mockito.verify(signature, Mockito.times(1)).getName();
    }
}
