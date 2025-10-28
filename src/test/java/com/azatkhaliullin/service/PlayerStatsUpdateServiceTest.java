package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.PlayerStats;
import com.azatkhaliullin.repository.PlayerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.ID_B;
import static com.azatkhaliullin.TestConstants.SCORE;
import static com.azatkhaliullin.builder.MatchResultTestBuilder.testMatchResult;
import static com.azatkhaliullin.builder.PlayerScoreTestBuilder.testPlayerScore;
import static com.azatkhaliullin.builder.PlayerStatsTestBuilder.testPlayerStats;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerStatsUpdateServiceTest {

    @Mock
    private PlayerRepository playerRepository;
    @InjectMocks
    private PlayerStatsUpdateService playerStatsUpdateService;

    @Nested
    @DisplayName("updatePlayerStats")
    class UpdatePlayerStatsTests {

        @Test
        void shouldUpdateStatsForAllPlayersInMatch() {
            MatchResult match = testMatchResult()
                    .withScores(List.of(
                            testPlayerScore().withId(ID_A).withScore(SCORE).build(),
                            testPlayerScore().withId(ID_B).withScore(SCORE).build()))
                    .build();

            PlayerStats currentStats1 = testPlayerStats().buildDomain();
            PlayerStats currentStats2 = testPlayerStats().buildDomain();

            when(playerRepository.findPlayerStats(ID_A)).thenReturn(currentStats1);
            when(playerRepository.findPlayerStats(ID_B)).thenReturn(currentStats2);

            playerStatsUpdateService.updatePlayerStats(match);

            verify(playerRepository).findPlayerStats(ID_A);
            verify(playerRepository).findPlayerStats(ID_B);
            verify(playerRepository).updatePlayerStats(eq(ID_A), any(PlayerStats.class));
            verify(playerRepository).updatePlayerStats(eq(ID_B), any(PlayerStats.class));
        }

        @Test
        void shouldHandleEmptyMatchScores() {
            MatchResult match = testMatchResult().withScores(List.of()).build();

            playerStatsUpdateService.updatePlayerStats(match);

            verify(playerRepository, never()).findPlayerStats(any());
            verify(playerRepository, never()).updatePlayerStats(any(), any());
        }

        @Test
        void shouldThrowExceptionWhenUpdateFails() {
            MatchResult match = testMatchResult()
                    .withScores(List.of(testPlayerScore().withId(ID_A).withScore(SCORE).build()))
                    .build();

            when(playerRepository.findPlayerStats(ID_A))
                    .thenThrow(new RuntimeException("ex"));

            assertThatThrownBy(() -> playerStatsUpdateService.updatePlayerStats(match))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ex");
        }
    }
}
