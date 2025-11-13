package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.Player;
import com.azatkhaliullin.dto.PlayerStatsDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static com.azatkhaliullin.TestConstants.AVERAGE_SCORE;
import static com.azatkhaliullin.TestConstants.BEST_SCORE;
import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.ID_B;
import static com.azatkhaliullin.TestConstants.SCORE;
import static com.azatkhaliullin.TestConstants.TOTAL_MATCHES;
import static com.azatkhaliullin.TestConstants.TOTAL_SCORE;
import static com.azatkhaliullin.TestConstants.USERNAME_A;
import static com.azatkhaliullin.TestConstants.WORST_SCORE;
import static com.azatkhaliullin.builder.MatchResultTestBuilder.testMatchResult;
import static com.azatkhaliullin.builder.PlayerScoreTestBuilder.testPlayerScore;
import static com.azatkhaliullin.builder.PlayerTestBuilder.testPlayer;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @InjectMocks
    private StatsService statsService;

    @Nested
    @DisplayName("calculatePlayerStats")
    class CalculatePlayerStatsTests {

        @Test
        void shouldCalculateStatsForPlayerWithMatches() {
            Player player = testPlayer().withId(ID_A).withUsername(USERNAME_A).build();

            List<MatchResult> matches = List.of(
                    testMatchResult().withScores(List.of(
                            testPlayerScore().withId(ID_A).withScore(BEST_SCORE).build())).build(),
                    testMatchResult().withScores(List.of(
                            testPlayerScore().withId(ID_A).withScore(WORST_SCORE).build())).build());

            PlayerStatsDto result = statsService.calculatePlayerStats(player, matches);

            assertThat(result.getUsername()).isEqualTo(USERNAME_A);
            assertThat(result.getTotalMatches()).isEqualTo(TOTAL_MATCHES);
            assertThat(result.getTotalScore()).isEqualTo(TOTAL_SCORE);
            assertThat(result.getBestScore()).isEqualTo(BEST_SCORE);
            assertThat(result.getWorstScore()).isEqualTo(WORST_SCORE);
            assertThat(result.getAverageScore()).isEqualTo(AVERAGE_SCORE);
            assertThat(result.getRating()).isPositive();
        }

        @Test
        void shouldCalculateStatsForPlayerWithNoMatches() {
            Player player = testPlayer().withUsername(USERNAME_A).build();

            PlayerStatsDto result = statsService.calculatePlayerStats(player, Collections.emptyList());

            assertThat(result.getUsername()).isEqualTo(USERNAME_A);
            assertThat(result.getTotalMatches()).isZero();
            assertThat(result.getTotalScore()).isZero();
            assertThat(result.getBestScore()).isZero();
            assertThat(result.getWorstScore()).isEqualTo(Integer.MAX_VALUE);
            assertThat(result.getAverageScore()).isZero();
            assertThat(result.getRating()).isZero();
        }

        @Test
        void shouldHandleMatchesWherePlayerNotParticipated() {
            Player player = testPlayer().withId(ID_A).withUsername(USERNAME_A).build();

            List<MatchResult> matches = List.of(
                    testMatchResult().withScores(List.of(
                            testPlayerScore().withId(ID_B).withScore(SCORE).build())).build());

            PlayerStatsDto result = statsService.calculatePlayerStats(player, matches);

            assertThat(result.getUsername()).isEqualTo(USERNAME_A);
            assertThat(result.getTotalMatches()).isZero();
            assertThat(result.getTotalScore()).isZero();
            assertThat(result.getBestScore()).isZero();
            assertThat(result.getWorstScore()).isEqualTo(Integer.MAX_VALUE);
            assertThat(result.getAverageScore()).isZero();
            assertThat(result.getRating()).isZero();
        }
    }
}
