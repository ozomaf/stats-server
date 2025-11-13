package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.UserCredentials;
import com.azatkhaliullin.dto.JwkKey;
import com.azatkhaliullin.dto.JwksResponse;
import com.azatkhaliullin.property.JwtProperties;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.INVALID_JWT_TOKEN;
import static com.azatkhaliullin.TestConstants.JWS_ALGORITHM;
import static com.azatkhaliullin.TestConstants.KEY_ID;
import static com.azatkhaliullin.TestConstants.PLAYER_ID_CLAIM;
import static com.azatkhaliullin.TestConstants.RSA_ALGORITHM;
import static com.azatkhaliullin.TestConstants.RSA_KEY_SIZE;
import static com.azatkhaliullin.TestConstants.TOKEN_EXPIRATION_SECONDS;
import static com.azatkhaliullin.TestConstants.USERNAME_A;
import static com.azatkhaliullin.builder.UserCredentialsTestBuilder.testUserCredentials;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceTest {

    @Mock
    private KeyPair jwtKeyPair;
    @Mock
    private JwtProperties jwtProperties;
    @InjectMocks
    private JwtTokenService jwtTokenService;

    @Nested
    @DisplayName("generateToken")
    class GenerateTokenTests {

        @Test
        void shouldGenerateValidToken() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            generator.initialize(RSA_KEY_SIZE);
            KeyPair realKeyPair = generator.generateKeyPair();

            UserCredentials userCredentials = testUserCredentials()
                    .withUsername(USERNAME_A)
                    .withPlayerId(ID_A)
                    .build();

            when(jwtProperties.getExpiration()).thenReturn(TOKEN_EXPIRATION_SECONDS);

            JwtTokenService service = new JwtTokenService(realKeyPair, jwtProperties);
            String token = service.generateToken(userCredentials);

            assertThat(token).isNotBlank();
            assertThat(token.split("\\.")).hasSize(3);

            Claims claims = service.validateAndGetClaims(token);
            assertThat(claims.getSubject()).isEqualTo(USERNAME_A);
            assertThat(UUID.fromString(claims.get(PLAYER_ID_CLAIM, String.class))).isEqualTo(ID_A);
            assertThat(claims.getExpiration()).isAfter(Date.from(Instant.now()));
        }

        @Test
        void shouldThrowExceptionWhenTokenGenerationFails() {
            UserCredentials userCredentials = testUserCredentials()
                    .withUsername(USERNAME_A)
                    .withPlayerId(ID_A)
                    .build();

            when(jwtProperties.getExpiration()).thenReturn(TOKEN_EXPIRATION_SECONDS);
            when(jwtKeyPair.getPrivate()).thenThrow(new RuntimeException());

            assertThatThrownBy(() -> jwtTokenService.generateToken(userCredentials))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Failed to generate JWT token");

            verify(jwtProperties).getExpiration();
            verify(jwtKeyPair).getPrivate();
        }
    }

    @Nested
    @DisplayName("validateAndGetClaims")
    class ValidateAndGetClaimsTests {

        @Test
        void shouldValidateValidToken() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            generator.initialize(RSA_KEY_SIZE);
            KeyPair realKeyPair = generator.generateKeyPair();

            UserCredentials userCredentials = testUserCredentials()
                    .withUsername(USERNAME_A)
                    .withPlayerId(ID_A)
                    .build();

            when(jwtProperties.getExpiration()).thenReturn(TOKEN_EXPIRATION_SECONDS);

            JwtTokenService service = new JwtTokenService(realKeyPair, jwtProperties);
            String token = service.generateToken(userCredentials);

            Claims claims = service.validateAndGetClaims(token);

            assertThat(claims).isNotNull();
            assertThat(claims.getSubject()).isEqualTo(USERNAME_A);
            assertThat(UUID.fromString(claims.get(PLAYER_ID_CLAIM, String.class))).isEqualTo(ID_A);
        }

        @Test
        void shouldThrowExceptionForInvalidToken() {
            when(jwtKeyPair.getPublic()).thenThrow(new RuntimeException());

            assertThatThrownBy(() -> jwtTokenService.validateAndGetClaims(INVALID_JWT_TOKEN))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid JWT token");
        }
    }

    @Nested
    @DisplayName("getJwks")
    class GetJwksTests {

        @Test
        void shouldReturnJwksResponse() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            generator.initialize(RSA_KEY_SIZE);
            KeyPair realKeyPair = generator.generateKeyPair();

            when(jwtProperties.getAlgorithm()).thenReturn(RSA_ALGORITHM);
            when(jwtProperties.getKeyId()).thenReturn(KEY_ID);
            when(jwtProperties.getJwsAlgorithm()).thenReturn(JWS_ALGORITHM);

            JwtTokenService service = new JwtTokenService(realKeyPair, jwtProperties);
            JwksResponse response = service.getJwks();

            assertThat(response).isNotNull();
            assertThat(response.getKeys()).isNotNull().hasSize(1);

            JwkKey key = response.getKeys().get(0);
            assertThat(key.getKty()).isEqualTo(RSA_ALGORITHM);
            assertThat(key.getKid()).isEqualTo(KEY_ID);
            assertThat(key.getAlg()).isEqualTo(JWS_ALGORITHM);
            assertThat(key.getUse()).isEqualTo(JwtTokenService.JWK_USAGE);
            assertThat(key.getN()).isNotBlank();
            assertThat(key.getE()).isNotBlank();

            verify(jwtProperties).getAlgorithm();
            verify(jwtProperties).getKeyId();
            verify(jwtProperties).getJwsAlgorithm();
        }
    }
}
