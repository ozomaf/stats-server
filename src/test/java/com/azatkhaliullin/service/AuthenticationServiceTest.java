package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.UserCredentials;
import com.azatkhaliullin.dto.LoginResponse;
import com.azatkhaliullin.property.JwtProperties;
import com.azatkhaliullin.repository.UserCredentialsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.azatkhaliullin.TestConstants.ACCESS_TOKEN;
import static com.azatkhaliullin.TestConstants.HASHED_PASSWORD;
import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.TOKEN_EXPIRATION_SECONDS;
import static com.azatkhaliullin.TestConstants.USERNAME_A;
import static com.azatkhaliullin.TestConstants.VALID_PASSWORD;
import static com.azatkhaliullin.builder.UserCredentialsTestBuilder.testUserCredentials;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserCredentialsRepository userCredentialsRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private JwtProperties jwtProperties;
    @InjectMocks
    private AuthenticationService authenticationService;

    @Nested
    @DisplayName("login")
    class LoginTests {

        @Test
        void shouldReturnLoginResponseWhenCredentialsAreValid() {
            UserCredentials userCredentials = testUserCredentials()
                    .withUsername(USERNAME_A)
                    .withHashedPassword(HASHED_PASSWORD)
                    .withPlayerId(ID_A)
                    .build();

            when(userCredentialsRepository.findByUsername(USERNAME_A))
                    .thenReturn(Optional.of(userCredentials));
            when(passwordEncoder.matches(VALID_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
            when(jwtTokenService.generateToken(userCredentials)).thenReturn(ACCESS_TOKEN);
            when(jwtProperties.getExpiration()).thenReturn(TOKEN_EXPIRATION_SECONDS);

            LoginResponse response = authenticationService.login(USERNAME_A, VALID_PASSWORD);

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.getExpiresIn()).isEqualTo(TOKEN_EXPIRATION_SECONDS);

            verify(userCredentialsRepository).findByUsername(USERNAME_A);
            verify(passwordEncoder).matches(VALID_PASSWORD, HASHED_PASSWORD);
            verify(jwtTokenService).generateToken(userCredentials);
            verify(jwtProperties).getExpiration();
            verifyNoMoreInteractions(userCredentialsRepository, passwordEncoder, jwtTokenService, jwtProperties);
        }

        @Test
        void shouldThrowExceptionWhenUsernameNotFound() {
            when(userCredentialsRepository.findByUsername(USERNAME_A))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.login(USERNAME_A, VALID_PASSWORD))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Username not found");

            verify(userCredentialsRepository).findByUsername(USERNAME_A);
            verify(passwordEncoder, never()).matches(any(), any());
            verify(jwtTokenService, never()).generateToken(any());
            verifyNoMoreInteractions(userCredentialsRepository, passwordEncoder, jwtTokenService, jwtProperties);
        }

        @Test
        void shouldThrowExceptionWhenPasswordIsInvalid() {
            UserCredentials userCredentials = testUserCredentials()
                    .withUsername(USERNAME_A)
                    .withHashedPassword(HASHED_PASSWORD)
                    .withPlayerId(ID_A)
                    .build();

            when(userCredentialsRepository.findByUsername(USERNAME_A))
                    .thenReturn(Optional.of(userCredentials));
            when(passwordEncoder.matches(VALID_PASSWORD, HASHED_PASSWORD)).thenReturn(false);

            assertThatThrownBy(() -> authenticationService.login(USERNAME_A, VALID_PASSWORD))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid login or password");

            verify(userCredentialsRepository).findByUsername(USERNAME_A);
            verify(passwordEncoder).matches(VALID_PASSWORD, HASHED_PASSWORD);
            verify(jwtTokenService, never()).generateToken(any());
            verifyNoMoreInteractions(userCredentialsRepository, passwordEncoder, jwtTokenService, jwtProperties);
        }
    }
}
