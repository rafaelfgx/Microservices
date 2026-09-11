package com.company.starter.validation.password;

import jakarta.validation.Constraint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

class PasswordAnnotationTest {
    private static final class AnnotatedClass {
        @Password
        private String password;
    }

    @Test
    void shouldExposeExpectedDefaultsWhenApplied() throws NoSuchFieldException {
        final var annotation = AnnotatedClass.class.getDeclaredField("password").getAnnotation(Password.class);
        Assertions.assertNotNull(annotation);
        Assertions.assertEquals("password.invalid", annotation.message());
        Assertions.assertEquals(0, annotation.groups().length);
        Assertions.assertEquals(0, annotation.payload().length);
    }

    @Test
    void shouldExposeExpectedMetaAnnotations() {
        Assertions.assertTrue(Password.class.isAnnotationPresent(Documented.class));

        final var constraint = Password.class.getAnnotation(Constraint.class);
        Assertions.assertNotNull(constraint);
        Assertions.assertArrayEquals(new Class[]{PasswordValidator.class}, constraint.validatedBy());

        final var target = Password.class.getAnnotation(Target.class);
        Assertions.assertNotNull(target);
        Assertions.assertTrue(Arrays.asList(target.value()).contains(ElementType.FIELD));
        Assertions.assertTrue(Arrays.asList(target.value()).contains(ElementType.PARAMETER));

        final var retention = Password.class.getAnnotation(Retention.class);
        Assertions.assertNotNull(retention);
        Assertions.assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }
}
