package com.company.starter.clients.auth;

import com.company.starter.clients.auth.dtos.UserRequest;
import com.company.starter.resilience.DefaultConcurrencyLimit;
import com.company.starter.resilience.DefaultRetryable;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Conditional(AuthCondition.class)
@DefaultConcurrencyLimit
@DefaultRetryable
@RequiredArgsConstructor
@Component
public class AuthAmbassador {
    private final AuthClient authClient;

    public ResponseEntity<UUID> save(final UserRequest request) {
        return authClient.save(request);
    }

    public ResponseEntity<Void> delete(final UUID id) {
        return authClient.delete(id);
    }
}
