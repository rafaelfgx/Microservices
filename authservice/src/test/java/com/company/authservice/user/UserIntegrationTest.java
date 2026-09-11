package com.company.authservice.user;

import com.company.authservice.shared.Data;
import com.company.authservice.shared.IntegrationTest;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class UserIntegrationTest extends IntegrationTest {
    @Test
    void shouldReturnCreatedWhenUserDoesNotExist() {
        Assertions.assertThat(save()).hasStatus(CREATED);
    }

    @Test
    void shouldReturnOkWhenUserAlreadyExists() {
        Assertions.assertThat(save()).hasStatus(CREATED);
        Assertions.assertThat(save()).hasStatus(OK);
    }

    @Test
    @SneakyThrows
    void shouldReturnNoContentWhenDeletingUser() {
        Assertions.assertThat(delete(UUID.randomUUID())).hasStatus(NO_CONTENT);
        final var result = save();
        Assertions.assertThat(result).hasStatus(CREATED);
        final var id = json.readValue(result.getResponse().getContentAsString(), UUID.class);
        Assertions.assertThat(delete(id)).hasStatus(NO_CONTENT);
    }

    private MvcTestResult save() {
        return mvc.post().uri("/users").header("Authorization", authorization()).contentType(APPLICATION_JSON).content(json.writeValueAsString(Data.USER_REQUEST)).exchange();
    }

    private MvcTestResult delete(final UUID id) {
        return mvc.delete().uri("/users/{id}", id).header("Authorization", authorization()).exchange();
    }
}
