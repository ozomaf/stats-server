package com.azatkhaliullin.builder;

import com.azatkhaliullin.domain.PlayerScore;

import java.util.UUID;

import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.SCORE;

public class PlayerScoreTestBuilder {

    private UUID id = ID_A;
    private int score = SCORE;

    public static PlayerScoreTestBuilder testPlayerScore() {
        return new PlayerScoreTestBuilder();
    }

    public PlayerScoreTestBuilder withId(UUID playerId) {
        this.id = playerId;
        return this;
    }

    public PlayerScoreTestBuilder withScore(int score) {
        this.score = score;
        return this;
    }

    public PlayerScore build() {
        return PlayerScore.builder()
                .playerId(id)
                .score(score)
                .build();
    }
}
