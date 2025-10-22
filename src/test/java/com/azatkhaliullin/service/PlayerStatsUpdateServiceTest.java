package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.PlayerStats;
import com.azatkhaliullin.mapper.PlayerStatsMapper;
import com.azatkhaliullin.repository.PlayerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.ID_B;
import static com.azatkhaliullin.TestConstants.SCORE;
import static com.azatkhaliullin.builder.MatchResultTestBuilder.testMatchResult;
import static com.azatkhaliullin.builder.PlayerScoreTestBuilder.testPlayerScore;
import static com.azatkhaliullin.builder.PlayerStatsTestBuilder.testPlayerStats;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerStatsUpdateServiceTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PlayerStatsMapper playerStatsMapper;
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

            Map<Object, Object> existingStats1 = testPlayerStats().buildMap();
            Map<Object, Object> existingStats2 = testPlayerStats().buildMap();

            PlayerStats currentStats1 = testPlayerStats().buildDomain();
            PlayerStats currentStats2 = testPlayerStats().buildDomain();

            when(playerRepository.findPlayerStats(ID_A)).thenReturn(existingStats1);
            when(playerRepository.findPlayerStats(ID_B)).thenReturn(existingStats2);
            when(playerStatsMapper.fromMap(existingStats1)).thenReturn(currentStats1);
            when(playerStatsMapper.fromMap(existingStats2)).thenReturn(currentStats2);

            playerStatsUpdateService.updatePlayerStats(match);

            verify(playerRepository).findPlayerStats(ID_A);
            verify(playerRepository).findPlayerStats(ID_B);
            verify(playerStatsMapper, times(2)).fromMap(anyMap());
            verify(playerStatsMapper, times(2)).toMap(any(PlayerStats.class));
            verify(playerRepository).updatePlayerStats(eq(ID_A), anyMap());
            verify(playerRepository).updatePlayerStats(eq(ID_B), anyMap());
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
