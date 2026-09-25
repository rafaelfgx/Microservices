package com.company.authservice.shared;

import com.company.authservice.auth.AuthRequest;
import com.company.authservice.auth.AuthResponse;
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

    protected String authorization;

    @BeforeEach
    void beforeEach() {
        KeycloakTestConfiguration.reset();
        authorization = "Bearer " + auth(Data.AUTH_REQUEST_VALID).assertThat().bodyJson().convertTo(AuthResponse.class).actual().accessToken();
    }

    protected MvcTestResult auth(final AuthRequest request) {
        return mvc.post().uri("/auth").contentType(APPLICATION_JSON).content(json.writeValueAsString(request)).exchange();
    }
}
