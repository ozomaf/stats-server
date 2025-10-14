package com.azatkhaliullin;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.Player;
import com.azatkhaliullin.domain.ServerInfo;
import com.azatkhaliullin.dto.MatchResultDto;
import com.azatkhaliullin.dto.PlayerStatsDto;
import com.azatkhaliullin.dto.ServerInfoDto;
import com.azatkhaliullin.dto.ServerStatsDto;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptyMap;

@UtilityClass
public final class TestDataFactory {

    public static final String PLAYER_A = "playerA";
    public static final String PLAYER_B = "playerB";

    public static final String UNKNOWN_SERVER = "unknown";
    public static final String SERVER_EU_ENDPOINT = "e1";
    public static final String SERVER_US_ENDPOINT = "e2";
    public static final String SERVER_GE_ENDPOINT = "e3";

    public static final OffsetDateTime PLAYED_AT = OffsetDateTime.parse("2025-10-10T12:00:00Z");

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(1);

    public static MatchResult matchResult(Map<String, Integer> scores) {
        return MatchResult.builder()
                .id(String.valueOf(ID_COUNTER.getAndIncrement()))
                .serverEndpoint("endpoint-" + ID_COUNTER.get())
                .playedAt(PLAYED_AT.toInstant())
                .scores(scores)
                .build();
    }

    public static MatchResult matchResult(String endpoint, Instant playedAt) {
        return MatchResult.builder()
                .id(String.valueOf(ID_COUNTER.getAndIncrement()))
                .serverEndpoint(endpoint)
                .playedAt(playedAt)
                .scores(emptyMap())
                .build();
    }

    public static MatchResultDto matchResultDto(String endpoint) {
        return MatchResultDto.builder()
                .id(String.valueOf(ID_COUNTER.getAndIncrement()))
                .serverEndpoint(endpoint)
                .scores(emptyMap())
                .playedAt(PLAYED_AT)
                .build();
    }

    public static List<MatchResult> matchesForPlayer(String player, int... scores) {
        return Arrays.stream(scores)
                .mapToObj(score -> matchResult(Map.of(player, score)))
                .toList();
    }

    public static ServerInfo serverInfo(String endpoint) {
        return ServerInfo.builder()
                .endpoint(endpoint)
                .name("name-" + endpoint)
                .region("region-" + endpoint)
                .build();
    }

    public static ServerInfoDto serverInfoDto(String endpoint) {
        return ServerInfoDto.builder()
                .endpoint(endpoint)
                .name("name-" + endpoint)
                .region("region-" + endpoint)
                .build();
    }

    public static ServerStatsDto serverStats(ServerInfo serverInfo, int totalMatches) {
        return ServerStatsDto.builder()
                .endpoint(serverInfo.getEndpoint())
                .totalMatches(totalMatches)
                .build();
    }

    public static ServerStatsDto serverStatsDto(String endpoint) {
        return ServerStatsDto.builder()
                .endpoint(endpoint)
                .totalMatches(1)
                .totalScore(10)
                .averagePlayersPerMatch(1)
                .averageScore(10.0)
                .build();
    }

    public static Player player(String username) {
        return Player.builder()
                .username(username)
                .build();
    }

    public static PlayerStatsDto playerStatsDto(String username) {
        return PlayerStatsDto.builder()
                .username(username)
                .totalMatches(1)
                .totalScore(10)
                .averageScore(10.0)
                .bestScore(10)
                .worstScore(10)
                .rating(5.0)
                .build();
    }
}
