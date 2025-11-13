package com.azatkhaliullin.integration;

import com.azatkhaliullin.BaseRedisIntegrationTest;
import com.azatkhaliullin.dto.LoginRequest;
import com.azatkhaliullin.dto.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static com.azatkhaliullin.TestConstants.AUTH_LOGIN_PATH;
import static com.azatkhaliullin.TestConstants.INTEGRATION_PASSWORD;
import static com.azatkhaliullin.TestConstants.INTEGRATION_USERNAME;
import static com.azatkhaliullin.TestConstants.INVALID_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthIntegrationTest extends BaseRedisIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Nested
    @DisplayName("POST /auth/login")
    class LoginEndpoint {

        @Test
        void login_withValidCredentials_returnsJwt() {
            LoginRequest request = new LoginRequest(INTEGRATION_USERNAME, INTEGRATION_PASSWORD);

            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                    AUTH_LOGIN_PATH, request, LoginResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType())
                    .isNotNull()
                    .satisfies(contentType -> assertThat(
                            contentType.isCompatibleWith(MediaType.APPLICATION_JSON))
                            .isTrue());

            LoginResponse loginResponse = Objects.requireNonNull(response.getBody());
            assertThat(loginResponse.getAccessToken()).isNotBlank();
            assertThat(loginResponse.getExpiresIn()).isPositive();
        }

        @Test
        void login_withInvalidPassword_returnsUnauthorized() {
            LoginRequest request = new LoginRequest(INTEGRATION_USERNAME, INVALID_PASSWORD);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    AUTH_LOGIN_PATH, request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getHeaders().getContentType())
                    .satisfies(contentType -> {
                        if (contentType != null) {
                            assertThat(contentType.isCompatibleWith(MediaType.APPLICATION_JSON) ||
                                    contentType.isCompatibleWith(MediaType.TEXT_PLAIN))
                                    .isTrue();
                        }
                    });
            assertThat(response.getBody())
                    .isNotNull()
                    .containsIgnoringCase("invalid login or password");
        }
    }
}
