package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.UserCredentials;
import com.azatkhaliullin.dto.JwkKey;
import com.azatkhaliullin.dto.JwksResponse;
import com.azatkhaliullin.property.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private static final String PLAYER_ID_CLAIM = "player_id";
    public static final String JWK_USAGE = "sig";

    private final KeyPair jwtKeyPair;
    private final JwtProperties jwtProperties;

    public String generateToken(UserCredentials userCredentials) {
        try {
            Instant now = Instant.now();
            Instant expirationTime = now.plusSeconds(jwtProperties.getExpiration());
            return Jwts.builder()
                    .id(UUID.randomUUID().toString())
                    .subject(userCredentials.getUsername())
                    .claim(PLAYER_ID_CLAIM, userCredentials.getPlayerId())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expirationTime))
                    .signWith(jwtKeyPair.getPrivate())
                    .compact();
        } catch (Exception e) {
            log.error("Failed to generate JWT token", e);
            throw new IllegalStateException("Failed to generate JWT token", e);
        }
    }

    public Claims validateAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(jwtKeyPair.getPublic())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public JwksResponse getJwks() {
        RSAPublicKey publicKey = (RSAPublicKey) jwtKeyPair.getPublic();

        JwkKey key = JwkKey.builder()
                .kty(jwtProperties.getAlgorithm())
                .kid(jwtProperties.getKeyId())
                .alg(jwtProperties.getJwsAlgorithm())
                .use(JWK_USAGE)
                .n(base64Url(publicKey.getModulus()))
                .e(base64Url(publicKey.getPublicExponent()))
                .build();

        return JwksResponse.builder()
                .keys(List.of(key))
                .build();
    }

    private static String base64Url(BigInteger value) {
        byte[] bytes = value.toByteArray();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
