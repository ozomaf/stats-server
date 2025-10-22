package com.azatkhaliullin.builder;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.PlayerScore;
import com.azatkhaliullin.dto.MatchResultDto;
import com.azatkhaliullin.mapper.MatchResultMapperImpl;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.azatkhaliullin.TestConstants.DEFAULT_PLAYED_AT;
import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.SCORES;
import static com.azatkhaliullin.TestConstants.SERVER_EU_ENDPOINT;

@RequiredArgsConstructor
public class MatchResultTestBuilder {

    private final MatchResultMapperImpl matchResultMapper = new MatchResultMapperImpl();

    private UUID id = ID_A;
    private String serverEndpoint = SERVER_EU_ENDPOINT;
    private Instant playedAt = DEFAULT_PLAYED_AT;
    private List<PlayerScore> scores = SCORES;

    public static MatchResultTestBuilder testMatchResult() {
        return new MatchResultTestBuilder();
    }

    public MatchResultTestBuilder withId(UUID playerId) {
        this.id = playerId;
        return this;
    }

    public MatchResultTestBuilder withServerEndpoint(String serverEndpoint) {
        this.serverEndpoint = serverEndpoint;
        return this;
    }

    public MatchResultTestBuilder withPlayedAt(Instant playedAt) {
        this.playedAt = playedAt;
        return this;
    }

    public MatchResultTestBuilder withScores(List<PlayerScore> scores) {
        this.scores = scores;
        return this;
    }

    public MatchResultDto buildDto() {
        return matchResultMapper.toDto(this.build());
    }

    public MatchResult build() {
        return MatchResult.builder()
                .id(id)
                .serverEndpoint(serverEndpoint)
                .playedAt(playedAt)
                .scores(scores)
                .build();
    }
}

