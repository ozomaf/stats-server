package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.dto.PlayerStatsDto;
import com.azatkhaliullin.repository.MatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.azatkhaliullin.TestDataFactory.PLAYER_A;
import static com.azatkhaliullin.TestDataFactory.PLAYER_B;
import static com.azatkhaliullin.TestDataFactory.matchesForPlayer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @InjectMocks
    private PlayerService playerService;

    @Nested
    @DisplayName("getPlayerStats")
    class GetPlayerStatsTests {

        @Test
        void shouldCalculateStatsAndRatingForPlayer() {
            List<MatchResult> matches = matchesForPlayer(PLAYER_A, 10, 20);
            when(matchRepository.getAllMatchesForPlayer(PLAYER_A)).thenReturn(matches);

            Optional<PlayerStatsDto> result = playerService.getPlayerStats(PLAYER_A);

            assertThat(result).isPresent().get().satisfies(stats -> {
                assertThat(stats.getUsername()).isEqualTo(PLAYER_A);
                assertThat(stats.getTotalMatches()).isEqualTo(2);
                assertThat(stats.getTotalScore()).isEqualTo(30);
                assertThat(stats.getAverageScore()).isEqualTo(15.0);
                assertThat(stats.getBestScore()).isEqualTo(20);
                assertThat(stats.getWorstScore()).isEqualTo(10);
                assertThat(stats.getRating()).isGreaterThan(0);
            });

            verify(matchRepository).getAllMatchesForPlayer(PLAYER_A);
            verifyNoMoreInteractions(matchRepository);
        }

        @Test
        void shouldHandlePlayerWithZeroMatches() {
            when(matchRepository.getAllMatchesForPlayer(PLAYER_A)).thenReturn(Collections.emptyList());

            Optional<PlayerStatsDto> result = playerService.getPlayerStats(PLAYER_A);

            assertThat(result).isPresent().get().satisfies(stats -> {
                assertThat(stats.getUsername()).isEqualTo(PLAYER_A);
                assertThat(stats.getTotalMatches()).isZero();
                assertThat(stats.getTotalScore()).isZero();
                assertThat(stats.getAverageScore()).isZero();
                assertThat(stats.getBestScore()).isZero();
                assertThat(stats.getWorstScore()).isZero();
                assertThat(stats.getRating()).isZero();
            });

            verify(matchRepository).getAllMatchesForPlayer(PLAYER_A);
            verifyNoMoreInteractions(matchRepository);
        }

        @Test
        void shouldPropagateRepositoryException() {
            when(matchRepository.getAllMatchesForPlayer(PLAYER_A))
                    .thenThrow(new RuntimeException("DB failure"));

            assertThatThrownBy(() -> playerService.getPlayerStats(PLAYER_A))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB failure");
        }
    }

    @Nested
    @DisplayName("getBestPlayers")
    class GetBestPlayersTests {

        @Test
        void shouldReturnTopNPlayersOrderedByRating() {
            List<MatchResult> matchesA = matchesForPlayer(PLAYER_A, 20, 10);
            List<MatchResult> matchesB = matchesForPlayer(PLAYER_B, 5, 5);
            List<MatchResult> allMatches = Stream.concat(matchesA.stream(), matchesB.stream()).toList();

            when(matchRepository.getAllMatches()).thenReturn(allMatches);

            List<PlayerStatsDto> topPlayers = playerService.getBestPlayers(2);

            assertThat(topPlayers)
                    .hasSize(2)
                    .extracting(PlayerStatsDto::getUsername).containsExactly(PLAYER_A, PLAYER_B);

            verify(matchRepository).getAllMatches();
            verify(matchRepository).getAllMatchesForPlayer(PLAYER_A);
            verify(matchRepository).getAllMatchesForPlayer(PLAYER_B);
        }

        @Test
        void shouldReturnEmptyForZeroCount() {
            List<PlayerStatsDto> result = playerService.getBestPlayers(0);
            assertThat(result).isEmpty();

            verify(matchRepository).getAllMatches();
            verifyNoMoreInteractions(matchRepository);
        }

        @Test
        void shouldReturnAllWhenCountGreaterThanPlayers() {
            List<MatchResult> matchesA = matchesForPlayer(PLAYER_A, 10);
            List<MatchResult> matchesB = matchesForPlayer(PLAYER_B, 20);
            List<MatchResult> allMatches = Stream.concat(matchesA.stream(), matchesB.stream()).toList();

            when(matchRepository.getAllMatches()).thenReturn(allMatches);

            List<PlayerStatsDto> result = playerService.getBestPlayers(10);

            assertThat(result)
                    .hasSize(2)
                    .extracting(PlayerStatsDto::getUsername)
                    .containsExactlyInAnyOrder(PLAYER_A, PLAYER_B);

            verify(matchRepository).getAllMatches();
            verify(matchRepository).getAllMatchesForPlayer(PLAYER_A);
            verify(matchRepository).getAllMatchesForPlayer(PLAYER_B);
        }
    }
}
