package com.company.authservice.shared;

import com.company.authservice.auth.AuthRequest;
import com.company.authservice.auth.AuthResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureMockMvc
@Import(SpringBootTestConfiguration.class)
@SpringBootTest
public abstract class IntegrationTest {
    @Autowired
    protected MockMvcTester mvc;

    @Autowired
    protected JsonMapper json;

    @BeforeEach
    void beforeEach() {
        KeycloakTestConfiguration.reset();
    }

    protected MvcTestResult auth(final AuthRequest request) {
        return mvc.post().uri("/auth").contentType(APPLICATION_JSON).content(json.writeValueAsString(request)).exchange();
    }

    @SneakyThrows
    protected String authorization() {
        return "Bearer " + json.readValue(auth(Data.AUTH_REQUEST_VALID).getResponse().getContentAsString(), AuthResponse.class).accessToken();
    }
}
