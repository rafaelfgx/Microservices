package com.company.authservice.user;

import com.company.authservice.shared.Data;
import com.company.authservice.shared.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.UUID;

class UserIntegrationTest extends IntegrationTest {
    @Test
    void shouldReturnSameIdWhenSameUserIsSavedAgain() {
        Assertions.assertThat(id(save(Data.SAVE_USER_REQUEST))).isEqualTo(id(save(Data.SAVE_USER_REQUEST)));
    }

    @Test
    void shouldReturnConflictWhenUsernameExistsWithAnotherEmail() {
        save(Data.SAVE_USER_REQUEST);
        Assertions.assertThat(save(Data.SAVE_USER_REQUEST.withEmail("another@mail.com"))).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturnConflictWhenEmailExistsWithAnotherUsername() {
        save(Data.SAVE_USER_REQUEST);
        Assertions.assertThat(save(Data.SAVE_USER_REQUEST.withUsername("another"))).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturnNoContentWhenDeletingUser() {
        Assertions.assertThat(delete(UUID.randomUUID())).hasStatus(HttpStatus.NO_CONTENT);
        Assertions.assertThat(delete(id(save(Data.SAVE_USER_REQUEST)))).hasStatus(HttpStatus.NO_CONTENT);
    }

    private MvcTestResult save(final SaveUserRequest request) {
        return mvc.post().uri("/users").header("Authorization", authorization).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)).exchange();
    }

    private MvcTestResult delete(final UUID id) {
        return mvc.delete().uri("/users/{id}", id).header("Authorization", authorization).exchange();
    }

    private UUID id(final MvcTestResult result) {
        return Assertions.assertThat(result).hasStatus(HttpStatus.OK).bodyJson().convertTo(UUID.class).actual();
    }
}
