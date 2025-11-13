package com.azatkhaliullin;

import com.azatkhaliullin.domain.PlayerScore;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class TestConstants {

    public static final String USERNAME_A = "testPlayerA";
    public static final String USERNAME_B = "testPlayerB";
    public static final String INVALID_USERNAME = " ";
    public static final String UNKNOWN_USER = "unknownUser";

    public static final String VALID_PASSWORD = "password123";
    public static final String INVALID_PASSWORD = "wrongPassword";
    public static final String ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiJ9.test.token";
    public static final String VALID_JWT_TOKEN = "valid.jwt.token";
    public static final String INVALID_JWT_TOKEN = "invalid.token.here";
    public static final String HASHED_PASSWORD = "$2a$10$hashedPasswordExample";
    public static final String ENCODED_PASSWORD = "$2a$10$encoded";
    public static final String PLAYER_ID_CLAIM = "player_id";
    public static final String RSA_ALGORITHM = "RSA";
    public static final String JWS_ALGORITHM = "RS256";
    public static final String KEY_ID = "test-key-id";
    public static final int RSA_KEY_SIZE = 2048;

    public static final long TOKEN_EXPIRATION_SECONDS = 3600L;

    public static final UUID ID_A = UUID.fromString("854d5a14-6e79-444d-af9c-d05951059193");
    public static final UUID ID_B = UUID.fromString("954d5a14-6e79-444d-af9c-d05951059194");

    public static final String SERVER_NAME = "testServerA";
    public static final String REGION = "region";
    public static final String SERVER_EU_ENDPOINT = "e1";
    public static final String SERVER_US_ENDPOINT = "e2";
    public static final String UNKNOWN_SERVER = "unknown";

    public static final Instant DEFAULT_PLAYED_AT = OffsetDateTime.parse("2025-10-10T12:00:00Z").toInstant();
    public static final long DEFAULT_TIMESTAMP = DEFAULT_PLAYED_AT.getEpochSecond();

    public static final int SCORE = 100;
    public static final List<PlayerScore> SCORES = List.of(new PlayerScore(ID_A, SCORE));

    public static final int TOTAL_MATCHES = 2;
    public static final int TOTAL_SCORE = 150;
    public static final int BEST_SCORE = 100;
    public static final int WORST_SCORE = 50;
    public static final double AVERAGE_SCORE = 75.0;
    public static final double RATING = 100.0;
    public static final int AVERAGE_PLAYER_PER_MATCH = 2;

    public static final int LIMIT = 2;

    public static final String AUTH_LOGIN_PATH = "/auth/login";
    public static final String JWKS_PATH = "/jwk/.well-known/jwks.json";
    public static final String GET_SERVER_INFO_PATH = "/servers/info/by-endpoint";
    public static final String GET_SERVER_MATCHES_SINCE_PATH = "/servers/matches/{timestamp}";
    public static final String GET_SERVERS_INFO_PATH = "/servers/info";
    public static final String GET_SERVER_STATS_PATH = "/servers/stats";
    public static final String GET_PLAYERS_USERNAME_STATS_PATH = "/players/{username}/stats";
    public static final String GET_RECENT_MATCHES_PATH = "/reports/recent-matches/{limit}";
    public static final String GET_BEST_PLAYERS_PATH = "/reports/best-players/{limit}";
    public static final String GET_POPULAR_SERVERS_PATH = "/reports/popular-servers/{limit}";
    public static final String PARAM_ENDPOINT = "endpoint";

    public static final String INTEGRATION_USERNAME = "DragonSlayer";
    public static final String INTEGRATION_PASSWORD = "1234";

}
