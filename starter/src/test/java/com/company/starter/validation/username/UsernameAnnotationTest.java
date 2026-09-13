package com.company.starter.validation.username;

import jakarta.validation.Constraint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

class UsernameAnnotationTest {
    private static final class AnnotatedClass {
        @Username
        private String username;
    }

    @Test
    void shouldExposeExpectedDefaultsWhenApplied() throws NoSuchFieldException {
        final var annotation = AnnotatedClass.class.getDeclaredField("username").getAnnotation(Username.class);
        Assertions.assertNotNull(annotation);
        Assertions.assertEquals("username.invalid", annotation.message());
        Assertions.assertEquals(0, annotation.groups().length);
        Assertions.assertEquals(0, annotation.payload().length);
    }

    @Test
    void shouldExposeExpectedMetaAnnotations() {
        Assertions.assertTrue(Username.class.isAnnotationPresent(Documented.class));

        final var constraint = Username.class.getAnnotation(Constraint.class);
        Assertions.assertNotNull(constraint);
        Assertions.assertArrayEquals(new Class[]{UsernameValidator.class}, constraint.validatedBy());

        final var target = Username.class.getAnnotation(Target.class);
        Assertions.assertNotNull(target);
        Assertions.assertTrue(Arrays.asList(target.value()).contains(ElementType.FIELD));
        Assertions.assertTrue(Arrays.asList(target.value()).contains(ElementType.PARAMETER));

        final var retention = Username.class.getAnnotation(Retention.class);
        Assertions.assertNotNull(retention);
        Assertions.assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }
}
