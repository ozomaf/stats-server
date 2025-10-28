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
    public static final int TOTAL_SCORE = 200;
    public static final int BEST_SCORE = 100;
    public static final int WORST_SCORE = 50;
    public static final double AVERAGE_SCORE = 75.0;
    public static final double RATING = 100.0;
    public static final int AVERAGE_PLAYER_PER_MATCH = 2;

    public static final int LIMIT = 2;

}
