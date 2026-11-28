package com.company.starter.otp;

import com.company.starter.MongoTestConfiguration;
import com.company.starter.exception.ApplicationException;
import jakarta.validation.ConstraintViolationException;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

@DataMongoTest
@ImportAutoConfiguration(ValidationAutoConfiguration.class)
@TestPropertySource(properties = {
    "spring.data.mongodb.auto-index-creation=true",
    "otp.expiration=PT5M",
    "otp.length=6",
    "otp.attempts=3"
})
@EnableConfigurationProperties(OtpProperties.class)
@Import({
    MongoTestConfiguration.class,
    OtpRepository.class,
    OtpService.class,
    OtpInitializer.class,
    OtpIntegrationTest.Configuration.class
})
class OtpIntegrationTest {
    private static final String EXPIRATION_INDEX = "otp_timestamp_ttl";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
        .withUserConfiguration(ConfigurationProperties.class);

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    OtpService service;

    @Autowired
    OtpRepository repository;

    @Autowired
    OtpInitializer initializer;

    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void beforeEach() {
        mongoTemplate.remove(new Query(), Otp.class);
    }

    @Test
    void shouldBindDefaultExpirationFromStarterConfiguration() throws Exception {
        final var sources = new YamlPropertySourceLoader().load("starter", new ClassPathResource("starter.yml"));
        contextRunner.withInitializer(context -> sources.forEach(context.getEnvironment().getPropertySources()::addLast))
            .run(context -> {
                Assertions.assertThat(context).hasNotFailed();
                Assertions.assertThat(context.getBean(OtpProperties.class).expiration()).isEqualTo(Duration.ofMinutes(5));
            });
    }

    @Test
    void shouldBindMinimumValidProperties() {
        contextRunner.withPropertyValues("otp.expiration=PT1S", "otp.length=1", "otp.attempts=1").run(context -> {
            Assertions.assertThat(context).hasNotFailed();
            final var properties = context.getBean(OtpProperties.class);
            Assertions.assertThat(properties.expiration()).isEqualTo(Duration.ofSeconds(1));
            Assertions.assertThat(properties.length()).isEqualTo(1);
            Assertions.assertThat(properties.attempts()).isEqualTo(1);
        });
    }

    @Test
    void shouldRejectMissingExpiration() {
        contextRunner.withPropertyValues("otp.length=6", "otp.attempts=3").run(context -> Assertions.assertThat(context).hasFailed());
    }

    @ParameterizedTest
    @ValueSource(strings = {"otp.expiration=PT0.999S", "otp.length=0", "otp.length=-1", "otp.attempts=0", "otp.attempts=-1"})
    void shouldRejectInvalidProperties(final String invalidProperty) {
        contextRunner
            .withPropertyValues("otp.expiration=PT5M", "otp.length=6", "otp.attempts=3")
            .withPropertyValues(invalidProperty)
            .run(context -> Assertions.assertThat(context).hasFailed());
    }

    @Test
    void shouldGeneratePersistAndConsumeCodeOnce() {
        final var code = service.generate(new GenerateOtpRequest("login", "user"));
        final var stored = mongoTemplate.findAll(Otp.class).getFirst();
        Assertions.assertThat(code).matches("[0-9]{6}");
        Assertions.assertThat(stored.getId()).isNotNull();
        Assertions.assertThat(stored.getTimestamp()).isNotNull();
        Assertions.assertThat(stored.getType()).isEqualTo("login");
        Assertions.assertThat(stored.getKey()).isEqualTo("user");
        Assertions.assertThat(stored.getAttempts()).isZero();
        Assertions.assertThat(stored.getHash()).isNotEqualTo(code);
        Assertions.assertThat(encoder.matches(code, stored.getHash())).isTrue();
        Assertions.assertThat(stored.toString()).doesNotContain(stored.getHash());
        service.validate(new ValidateOtpRequest("login", "user", code));
        assertInvalid("login", "user", code);
        Assertions.assertThat(mongoTemplate.findAll(Otp.class)).isEmpty();
    }

    @Test
    void shouldReplaceCodeAndResetAttemptsWithoutChangingIdentity() {
        service.generate(new GenerateOtpRequest("login", "user"));
        final var original = mongoTemplate.findAll(Otp.class).getFirst();
        assertInvalid("login", "user", "incorrect");
        final var replacement = service.generate(new GenerateOtpRequest("login", "user"));
        final var stored = mongoTemplate.findAll(Otp.class).getFirst();
        Assertions.assertThat(stored.getId()).isEqualTo(original.getId());
        Assertions.assertThat(stored.getTimestamp()).isAfterOrEqualTo(original.getTimestamp());
        Assertions.assertThat(stored.getHash()).isNotEqualTo(original.getHash());
        Assertions.assertThat(stored.getAttempts()).isZero();
        Assertions.assertThat(encoder.matches(replacement, stored.getHash())).isTrue();
        service.validate(new ValidateOtpRequest("login", "user", replacement));
    }

    @Test
    void shouldIsolateTypesAndKeys() {
        final var loginCode = service.generate(new GenerateOtpRequest("login", "user"));
        final var resetCode = service.generate(new GenerateOtpRequest("reset", "user"));
        final var otherCode = service.generate(new GenerateOtpRequest("login", "other"));
        assertInvalid("missing", "user", loginCode);
        assertInvalid("login", "missing", loginCode);
        service.validate(new ValidateOtpRequest("login", "user", loginCode));
        service.validate(new ValidateOtpRequest("reset", "user", resetCode));
        service.validate(new ValidateOtpRequest("login", "other", otherCode));
    }

    @Test
    void shouldAllowLastAttemptAndRejectFurtherAttempts() {
        final var code = service.generate(new GenerateOtpRequest("login", "user"));

        for (int attempt = 0; attempt < 2; attempt++) {
            assertInvalid("login", "user", "wrong");
        }

        service.validate(new ValidateOtpRequest("login", "user", code));

        final var nextCode = service.generate(new GenerateOtpRequest("login", "user"));

        for (int attempt = 0; attempt < 3; attempt++) {
            assertInvalid("login", "user", "wrong");
        }

        assertInvalid("login", "user", nextCode);
        Assertions.assertThat(mongoTemplate.findAll(Otp.class).getFirst().getAttempts()).isEqualTo(3);
    }

    @Test
    void shouldRejectExpiredCodeBeforeTtlCleanup() {
        final var code = service.generate(new GenerateOtpRequest("login", "user"));
        mongoTemplate.indexOps(Otp.class).dropIndex(EXPIRATION_INDEX);

        try {
            mongoTemplate.updateFirst(new Query(), new Update().set("timestamp", Instant.now().minus(Duration.ofMinutes(6))), Otp.class);
            assertInvalid("login", "user", code);
            Assertions.assertThat(mongoTemplate.findAll(Otp.class).getFirst().getAttempts()).isZero();
        } finally {
            initializer.run(null);
        }
    }

    @Test
    void shouldEnforceExclusiveExpirationBoundaryAndReturnPreviousAttemptCount() {
        repository.upsert("login", "user", "hash");
        final var timestamp = Instant.now().plusSeconds(120).truncatedTo(ChronoUnit.MILLIS);
        mongoTemplate.updateFirst(new Query(), new Update().set("timestamp", timestamp), Otp.class);
        Assertions.assertThat(repository.consume("login", "user", timestamp, 3)).isEmpty();
        final var attempt = repository.consume("login", "user", timestamp.minusSeconds(1), 3);
        Assertions.assertThat(attempt).hasValueSatisfying(otp -> Assertions.assertThat(otp.getAttempts()).isZero());
        Assertions.assertThat(mongoTemplate.findAll(Otp.class).getFirst().getAttempts()).isEqualTo(1);
    }

    @Test
    void shouldReturnFalseWhenDeletingMissingId() {
        Assertions.assertThat(repository.delete(UUID.randomUUID())).isFalse();
    }

    @Test
    void shouldEnforceUniqueTypeAndKeyIndex() {
        repository.upsert("login", "user", "hash");
        final var duplicate = new Document("_id", UUID.randomUUID()).append("type", "login").append("key", "user");
        Assertions.assertThatThrownBy(() -> mongoTemplate.insert(duplicate, mongoTemplate.getCollectionName(Otp.class))).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void shouldCreateAndUpdateExpirationIndex() {
        final var operations = mongoTemplate.indexOps(Otp.class);
        operations.dropIndex(EXPIRATION_INDEX);

        try {
            initializer.run(null);
            assertExpirationIndex();
            operations.dropIndex(EXPIRATION_INDEX);
            operations.createIndex(new Index("timestamp", Sort.Direction.ASC).named(EXPIRATION_INDEX).expire(Duration.ofHours(2)));
            initializer.run(null);
            assertExpirationIndex();
        } finally {
            initializer.run(null);
        }
    }

    @Test
    void shouldConsumeCodeAtMostOnceConcurrently() throws Exception {
        final var code = service.generate(new GenerateOtpRequest("login", "user"));
        final var start = new CountDownLatch(1);

        final Callable<Boolean> validate = () -> {
            start.await();
            try {
                service.validate(new ValidateOtpRequest("login", "user", code));
                return true;
            } catch (final ApplicationException exception) {
                Assertions.assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                Assertions.assertThat(exception.getBody().getDetail()).isEqualTo("OTP is invalid or expired");
                return false;
            }
        };

        try (final var executor = Executors.newFixedThreadPool(2)) {
            final var first = executor.submit(validate);
            final var second = executor.submit(validate);
            start.countDown();
            Assertions.assertThat(first.get() ^ second.get()).isTrue();
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldRejectBlankInputs(final String invalid) {
        Assertions.assertThatThrownBy(() -> service.generate(new GenerateOtpRequest(invalid, "user"))).isInstanceOf(ConstraintViolationException.class);
        Assertions.assertThatThrownBy(() -> service.generate(new GenerateOtpRequest("login", invalid))).isInstanceOf(ConstraintViolationException.class);
        Assertions.assertThatThrownBy(() -> service.validate(new ValidateOtpRequest(invalid, "user", "123456"))).isInstanceOf(ConstraintViolationException.class);
        Assertions.assertThatThrownBy(() -> service.validate(new ValidateOtpRequest("login", invalid, "123456"))).isInstanceOf(ConstraintViolationException.class);
        Assertions.assertThatThrownBy(() -> service.validate(new ValidateOtpRequest("login", "user", invalid))).isInstanceOf(ConstraintViolationException.class);
        Assertions.assertThat(mongoTemplate.findAll(Otp.class)).isEmpty();
    }

    private void assertInvalid(final String type, final String key, final String code) {
        Assertions.assertThatThrownBy(() -> service.validate(new ValidateOtpRequest(type, key, code)))
            .isInstanceOfSatisfying(ApplicationException.class, exception -> {
                Assertions.assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                Assertions.assertThat(exception.getBody().getDetail()).isEqualTo("OTP is invalid or expired");
            });
    }

    private void assertExpirationIndex() {
        Assertions
            .assertThat(mongoTemplate.indexOps(Otp.class).getIndexInfo())
            .filteredOn(index -> EXPIRATION_INDEX.equals(index.getName()))
            .singleElement()
            .satisfies(index -> Assertions.assertThat(index.getExpireAfter()).contains(Duration.ofMinutes(5)));
    }

    @TestConfiguration
    static class Configuration {
        @Bean
        PasswordEncoder passwordEncoder() {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableConfigurationProperties(OtpProperties.class)
    static class ConfigurationProperties {
    }
}
