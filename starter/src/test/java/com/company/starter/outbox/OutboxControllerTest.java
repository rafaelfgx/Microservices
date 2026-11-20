package com.company.starter.outbox;

import com.company.starter.MongoTestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@DataMongoTest
@Import({MongoTestConfiguration.class, OutboxController.class})
class OutboxControllerTest {
    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    OutboxController outboxController;

    MockMvcTester mvc;

    @BeforeEach
    void beforeEach() {
        mvc = MockMvcTester.create(MockMvcBuilders.standaloneSetup(outboxController).setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build());
        mongoTemplate.dropCollection(Outbox.class);
    }

    @Test
    void shouldFindAll() {
        mongoTemplate.save(new Outbox("topic", "key", "{}"));
        mongoTemplate.save(new Outbox("topic", "key", "{}"));
        final var result = mvc.get().uri("/outbox?page=0&size=1").exchange();
        Assertions.assertThat(result).hasStatus(OK).bodyJson().extractingPath("$.content.length()").isEqualTo(1);
        Assertions.assertThat(result).bodyJson().extractingPath("$.totalElements").isEqualTo(2);
    }

    @Test
    void shouldFindById() {
        final var outbox = mongoTemplate.save(new Outbox("topic", "key", "{}"));
        Assertions.assertThat(mvc.get().uri("/outbox/{id}", outbox.getId()).exchange()).hasStatus(OK).bodyJson().extractingPath("$.topic").isEqualTo("topic");
    }

    @Test
    void shouldReturnNotFoundWhenIdDoesNotExist() {
        Assertions.assertThat(mvc.get().uri("/outbox/{id}", UUID.randomUUID()).exchange()).hasStatus(NOT_FOUND);
    }

    @Test
    void shouldFindByTopic() {
        mongoTemplate.save(new Outbox("topic1", "key", "{}"));
        mongoTemplate.save(new Outbox("topic2", "key", "{}"));
        Assertions.assertThat(mvc.get().uri("/outbox/topics/{topic}", "topic1").exchange()).hasStatus(OK).bodyJson().extractingPath("$.content.length()").isEqualTo(1);
    }

    @Test
    void shouldFindByKey() {
        mongoTemplate.save(new Outbox("topic", "key1", "{}"));
        mongoTemplate.save(new Outbox("topic", "key2", "{}"));
        Assertions.assertThat(mvc.get().uri("/outbox/keys/{key}", "key1").exchange()).hasStatus(OK).bodyJson().extractingPath("$.content.length()").isEqualTo(1);
    }

    @Test
    void shouldFindByAttempts() {
        mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 1, null, null));
        mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 3, null, null));
        mongoTemplate.save(new Outbox(UUID.randomUUID(), Instant.now(), "topic", "key", "{}", 5, null, null));
        Assertions.assertThat(mvc.get().uri("/outbox/attempts/{attempts}", 3).exchange()).hasStatus(OK).bodyJson().extractingPath("$.content.length()").isEqualTo(2);
    }

    @Test
    void shouldDelete() {
        final var outbox = mongoTemplate.save(new Outbox("topic", "key", "{}"));
        Assertions.assertThat(mvc.delete().uri("/outbox/{id}", outbox.getId()).exchange()).hasStatus(NO_CONTENT);
        Assertions.assertThat(mongoTemplate.findAll(Outbox.class)).isEmpty();
    }

    @Test
    void shouldReturnNoContentWhenDeletingMissingId() {
        Assertions.assertThat(mvc.delete().uri("/outbox/{id}", UUID.randomUUID()).exchange()).hasStatus(NO_CONTENT);
    }

    @Test
    void shouldSave() {
        final var result = mvc.post().uri("/outbox").contentType(APPLICATION_JSON).content("{\"topic\":\"topic\",\"key\":\"key\",\"data\":\"{}\"}").exchange();
        Assertions.assertThat(result).hasStatus(OK);
        Assertions.assertThat(mongoTemplate.findAll(Outbox.class)).hasSize(1);
    }

    @Test
    void shouldReturnBadRequestWhenInvalid() {
        final var result = mvc.post().uri("/outbox").contentType(APPLICATION_JSON).content("{}").exchange();
        Assertions.assertThat(result).hasStatus(BAD_REQUEST);
        Assertions.assertThat(mongoTemplate.findAll(Outbox.class)).isEmpty();
    }
}
