package com.azatkhaliullin.controller;

import com.azatkhaliullin.dto.LoginResponse;
import com.azatkhaliullin.service.AuthenticationService;
import com.azatkhaliullin.service.JwtTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.azatkhaliullin.TestConstants.ACCESS_TOKEN;
import static com.azatkhaliullin.TestConstants.AUTH_LOGIN_PATH;
import static com.azatkhaliullin.TestConstants.INVALID_PASSWORD;
import static com.azatkhaliullin.TestConstants.TOKEN_EXPIRATION_SECONDS;
import static com.azatkhaliullin.TestConstants.UNKNOWN_USER;
import static com.azatkhaliullin.TestConstants.USERNAME_A;
import static com.azatkhaliullin.TestConstants.VALID_PASSWORD;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Nested
    @DisplayName("POST /auth/login")
    class LoginTests {

        @Test
        void shouldReturnLoginResponseWhenCredentialsAreValid() throws Exception {
            LoginResponse response = LoginResponse.builder()
                    .accessToken(ACCESS_TOKEN)
                    .expiresIn(TOKEN_EXPIRATION_SECONDS)
                    .build();

            when(authenticationService.login(USERNAME_A, VALID_PASSWORD))
                    .thenReturn(response);

            mockMvc.perform(post(AUTH_LOGIN_PATH)
                            .contentType(APPLICATION_JSON)
                            .content("{\"username\":\"" + USERNAME_A + "\",\"password\":\"" + VALID_PASSWORD + "\"}")
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
                    .andExpect(jsonPath("$.expiresIn").value(TOKEN_EXPIRATION_SECONDS));

            verify(authenticationService).login(USERNAME_A, VALID_PASSWORD);
            verifyNoMoreInteractions(authenticationService);
        }

        @Test
        void shouldReturn500WhenServiceThrowsException() throws Exception {
            when(authenticationService.login(USERNAME_A, INVALID_PASSWORD))
                    .thenThrow(new IllegalArgumentException());

            mockMvc.perform(post(AUTH_LOGIN_PATH)
                            .contentType(APPLICATION_JSON)
                            .content("{\"username\":\"" + USERNAME_A + "\",\"password\":\"" + INVALID_PASSWORD + "\"}")
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(authenticationService).login(USERNAME_A, INVALID_PASSWORD);
            verifyNoMoreInteractions(authenticationService);
        }

        @Test
        void shouldReturn500WhenUsernameNotFound() throws Exception {
            when(authenticationService.login(UNKNOWN_USER, VALID_PASSWORD))
                    .thenThrow(new IllegalArgumentException());

            mockMvc.perform(post(AUTH_LOGIN_PATH)
                            .contentType(APPLICATION_JSON)
                            .content("{\"username\":\"" + UNKNOWN_USER + "\",\"password\":\"" + VALID_PASSWORD + "\"}")
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(authenticationService).login(UNKNOWN_USER, VALID_PASSWORD);
            verifyNoMoreInteractions(authenticationService);
        }
    }
}
