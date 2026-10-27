package com.company.starter.clients.auth;

import com.company.starter.clients.auth.dtos.SaveUserRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

class AuthAmbassadorTest {
    final AuthClient authClient = Mockito.mock(AuthClient.class);
    final AuthAmbassador authAmbassador = new AuthAmbassador(authClient);
    final SaveUserRequest saveUserRequest = new SaveUserRequest("Name", "email@mail.com", "username", "password");

    @Test
    void shouldSave() {
        final var expected = ResponseEntity.ok(UUID.randomUUID());
        Mockito.when(authClient.save(saveUserRequest)).thenReturn(expected);
        final var response = authAmbassador.save(saveUserRequest);
        Assertions.assertSame(expected, response);
        Mockito.verify(authClient).save(saveUserRequest);
    }

    @Test
    void shouldDelete() {
        final var id = UUID.randomUUID();
        final ResponseEntity<Void> expected = ResponseEntity.noContent().build();
        Mockito.when(authClient.delete(id)).thenReturn(expected);
        final var response = authAmbassador.delete(id);
        Assertions.assertSame(expected, response);
        Mockito.verify(authClient).delete(id);
    }
}
