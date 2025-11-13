package com.azatkhaliullin.mapper;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.PlayerScore;
import com.azatkhaliullin.dto.MatchResultDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZoneOffset;
import java.util.List;

import static com.azatkhaliullin.TestConstants.DEFAULT_PLAYED_AT;
import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.SCORE;
import static com.azatkhaliullin.TestConstants.SERVER_EU_ENDPOINT;
import static com.azatkhaliullin.builder.PlayerScoreTestBuilder.testPlayerScore;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MatchResultMapperTest {

    @Autowired
    private MatchResultMapper matchResultMapper;

    @Nested
    @DisplayName("toDto")
    class ToDtoTests {

        @Test
        void shouldMapMatchResultToDto() {
            PlayerScore playerScore = testPlayerScore()
                    .withId(ID_A)
                    .withScore(SCORE)
                    .build();

            MatchResult matchResult = MatchResult.builder()
                    .id(ID_A)
                    .serverEndpoint(SERVER_EU_ENDPOINT)
                    .playedAt(DEFAULT_PLAYED_AT)
                    .scores(List.of(playerScore))
                    .build();

            MatchResultDto dto = matchResultMapper.toDto(matchResult);

            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(ID_A);
            assertThat(dto.getServerEndpoint()).isEqualTo(SERVER_EU_ENDPOINT);
            assertThat(dto.getPlayedAt()).isEqualTo(DEFAULT_PLAYED_AT.atOffset(ZoneOffset.UTC));
            assertThat(dto.getScores()).hasSize(1);
            assertThat(dto.getScores().get(0).getPlayerId()).isEqualTo(ID_A);
            assertThat(dto.getScores().get(0).getScore()).isEqualTo(SCORE);
        }

        @Test
        void shouldMapNullInstantToNull() {
            MatchResult matchResult = MatchResult.builder()
                    .id(ID_A)
                    .serverEndpoint(SERVER_EU_ENDPOINT)
                    .playedAt(null)
                    .scores(List.of())
                    .build();

            MatchResultDto dto = matchResultMapper.toDto(matchResult);

            assertThat(dto).isNotNull();
            assertThat(dto.getPlayedAt()).isNull();
        }

        @Test
        void shouldMapEmptyScoresList() {
            MatchResult matchResult = MatchResult.builder()
                    .id(ID_A)
                    .serverEndpoint(SERVER_EU_ENDPOINT)
                    .playedAt(DEFAULT_PLAYED_AT)
                    .scores(List.of())
                    .build();

            MatchResultDto dto = matchResultMapper.toDto(matchResult);

            assertThat(dto).isNotNull();
            assertThat(dto.getScores()).isEmpty();
        }
    }
}

