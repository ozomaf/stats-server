package com.azatkhaliullin.builder;

import com.azatkhaliullin.dto.ServerStatsDto;

import static com.azatkhaliullin.TestConstants.AVERAGE_PLAYER_PER_MATCH;
import static com.azatkhaliullin.TestConstants.AVERAGE_SCORE;
import static com.azatkhaliullin.TestConstants.REGION;
import static com.azatkhaliullin.TestConstants.SERVER_EU_ENDPOINT;
import static com.azatkhaliullin.TestConstants.SERVER_NAME;
import static com.azatkhaliullin.TestConstants.TOTAL_MATCHES;
import static com.azatkhaliullin.TestConstants.TOTAL_SCORE;

public class ServerStatsDtoBuilder {

    private String endpoint = SERVER_EU_ENDPOINT;
    private String name = SERVER_NAME;
    private String region = REGION;
    private Integer totalMatches = TOTAL_MATCHES;
    private Integer averagePlayersPerMatch = AVERAGE_PLAYER_PER_MATCH;
    private Integer totalScore = TOTAL_SCORE;
    private Double averageScore = AVERAGE_SCORE;

    public static ServerStatsDtoBuilder testServerStatsDto() {
        return new ServerStatsDtoBuilder();
    }

    public ServerStatsDtoBuilder withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public ServerStatsDtoBuilder withTotalMatches(Integer totalMatches) {
        this.totalMatches = totalMatches;
        return this;
    }

    public ServerStatsDto build() {
        return ServerStatsDto.builder()
                .endpoint(endpoint)
                .name(name)
                .region(region)
                .totalMatches(totalMatches)
                .averagePlayersPerMatch(averagePlayersPerMatch)
                .totalScore(totalScore)
                .averageScore(averageScore)
                .build();
    }
}
