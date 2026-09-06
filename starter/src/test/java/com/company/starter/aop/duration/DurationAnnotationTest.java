package com.company.starter.aop.duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

class DurationAnnotationTest {
    private static final class AnnotatedClass {
        @Duration
        void annotatedMethod() {
        }
    }

    @Test
    void shouldExposeExpectedMetaAnnotations() throws NoSuchMethodException {
        final var annotation = AnnotatedClass.class.getDeclaredMethod("annotatedMethod").getAnnotation(Duration.class);
        Assertions.assertNotNull(annotation);

        final var target = Duration.class.getAnnotation(Target.class);
        Assertions.assertNotNull(target);
        Assertions.assertEquals(1, target.value().length);
        Assertions.assertTrue(Arrays.asList(target.value()).contains(ElementType.METHOD));

        final var retention = Duration.class.getAnnotation(Retention.class);
        Assertions.assertNotNull(retention);
        Assertions.assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }
}
