package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.Player;
import com.azatkhaliullin.domain.PlayerScore;
import com.azatkhaliullin.dto.PlayerStatsDto;
import com.azatkhaliullin.repository.MatchRepository;
import com.azatkhaliullin.repository.PlayerRepository;
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

import static com.azatkhaliullin.TestConstants.AVERAGE_SCORE;
import static com.azatkhaliullin.TestConstants.BEST_SCORE;
import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.ID_B;
import static com.azatkhaliullin.TestConstants.RATING;
import static com.azatkhaliullin.TestConstants.SCORE;
import static com.azatkhaliullin.TestConstants.TOTAL_MATCHES;
import static com.azatkhaliullin.TestConstants.TOTAL_SCORE;
import static com.azatkhaliullin.TestConstants.USERNAME_A;
import static com.azatkhaliullin.TestConstants.USERNAME_B;
import static com.azatkhaliullin.TestConstants.WORST_SCORE;
import static com.azatkhaliullin.builder.MatchResultTestBuilder.testMatchResult;
import static com.azatkhaliullin.builder.PlayerScoreTestBuilder.testPlayerScore;
import static com.azatkhaliullin.builder.PlayerStatsTestBuilder.emptyPlayerStats;
import static com.azatkhaliullin.builder.PlayerStatsTestBuilder.testPlayerStats;
import static com.azatkhaliullin.builder.PlayerTestBuilder.testPlayer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private StatsService statsService;
    @InjectMocks
    private PlayerService playerService;

    @Nested
    @DisplayName("getPlayerStats")
    class GetPlayerStatsTests {

        @Test
        void shouldCalculateStatsAndRatingForPlayer() {
            Player player = testPlayer().withId(ID_A).withUsername(USERNAME_A).build();
            PlayerScore playerScore = testPlayerScore().withId(ID_A).withScore(SCORE).build();

            List<MatchResult> matches = List.of(
                    testMatchResult().withScores(List.of(playerScore)).build());

            PlayerStatsDto expectedStats = testPlayerStats().withUsername(USERNAME_A).buildDto();

            when(playerRepository.findByUsername(USERNAME_A)).thenReturn(Optional.of(player));
            when(matchRepository.findByPlayerId(ID_A)).thenReturn(matches);
            when(statsService.calculatePlayerStats(player, matches)).thenReturn(expectedStats);

            Optional<PlayerStatsDto> result = playerService.getPlayerStats(USERNAME_A);

            assertThat(result).isPresent().get().satisfies(stats -> {
                assertThat(stats.getUsername()).isEqualTo(USERNAME_A);
                assertThat(stats.getTotalMatches()).isEqualTo(TOTAL_MATCHES);
                assertThat(stats.getTotalScore()).isEqualTo(TOTAL_SCORE);
                assertThat(stats.getAverageScore()).isEqualTo(AVERAGE_SCORE);
                assertThat(stats.getBestScore()).isEqualTo(BEST_SCORE);
                assertThat(stats.getWorstScore()).isEqualTo(WORST_SCORE);
                assertThat(stats.getRating()).isEqualTo(RATING);
            });

            verify(playerRepository).findByUsername(USERNAME_A);
            verify(matchRepository).findByPlayerId(ID_A);
            verify(statsService).calculatePlayerStats(player, matches);
            verifyNoMoreInteractions(playerRepository, matchRepository, statsService);
        }

        @Test
        void shouldHandlePlayerWithZeroMatches() {
            Player player = testPlayer().withId(ID_A).withUsername(USERNAME_A).build();
            PlayerStatsDto expectedStats = emptyPlayerStats().withUsername(USERNAME_A).buildDto();

            when(playerRepository.findByUsername(USERNAME_A)).thenReturn(Optional.of(player));
            when(matchRepository.findByPlayerId(ID_A)).thenReturn(Collections.emptyList());
            when(statsService.calculatePlayerStats(player, Collections.emptyList())).thenReturn(expectedStats);

            Optional<PlayerStatsDto> result = playerService.getPlayerStats(USERNAME_A);

            assertThat(result).isPresent().get().satisfies(stats -> {
                assertThat(stats.getUsername()).isEqualTo(USERNAME_A);
                assertThat(stats.getTotalMatches()).isZero();
                assertThat(stats.getTotalScore()).isZero();
                assertThat(stats.getAverageScore()).isZero();
                assertThat(stats.getBestScore()).isZero();
                assertThat(stats.getWorstScore()).isEqualTo(Integer.MAX_VALUE);
                assertThat(stats.getRating()).isZero();
            });

            verify(playerRepository).findByUsername(USERNAME_A);
            verify(matchRepository).findByPlayerId(ID_A);
            verify(statsService).calculatePlayerStats(player, Collections.emptyList());
            verifyNoMoreInteractions(playerRepository, matchRepository, statsService);
        }

        @Test
        void shouldThrowRepositoryException() {
            when(playerRepository.findByUsername(USERNAME_A))
                    .thenThrow(new RuntimeException("ex"));

            assertThatThrownBy(() -> playerService.getPlayerStats(USERNAME_A))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ex");
        }
    }

    @Nested
    @DisplayName("getBestPlayers")
    class GetBestPlayersTests {

        @Test
        void shouldReturnTopNPlayersOrderedByRating() {
            Player playerA = testPlayer().withId(ID_A).withUsername(USERNAME_A).build();
            Player playerB = testPlayer().withId(ID_B).withUsername(USERNAME_B).build();
            List<Player> players = List.of(playerA, playerB);

            PlayerScore playerScoreA = testPlayerScore().withId(ID_A).withScore(SCORE).build();
            PlayerScore playerScoreB = testPlayerScore().withId(ID_B).withScore(SCORE).build();

            List<MatchResult> matchesA = List.of(
                    testMatchResult().withScores(List.of(playerScoreA)).build());
            List<MatchResult> matchesB = List.of(
                    testMatchResult().withScores(List.of(playerScoreB)).build());


            PlayerStatsDto statsA = testPlayerStats()
                    .withUsername(USERNAME_A)
                    .withRating(RATING + RATING)
                    .buildDto();

            PlayerStatsDto statsB = testPlayerStats()
                    .withUsername(USERNAME_B)
                    .withRating(RATING)
                    .buildDto();

            when(playerRepository.findAll()).thenReturn(players);
            when(matchRepository.findByPlayerId(ID_A)).thenReturn(matchesA);
            when(matchRepository.findByPlayerId(ID_B)).thenReturn(matchesB);
            when(statsService.calculatePlayerStats(playerA, matchesA)).thenReturn(statsA);
            when(statsService.calculatePlayerStats(playerB, matchesB)).thenReturn(statsB);

            List<PlayerStatsDto> topPlayers = playerService.getBestPlayers(2);

            assertThat(topPlayers)
                    .hasSize(2)
                    .extracting(PlayerStatsDto::getUsername)
                    .containsExactly(USERNAME_A, USERNAME_B);

            verify(playerRepository).findAll();
            verify(matchRepository, times(2)).findByPlayerId(any());
            verify(statsService, times(2)).calculatePlayerStats(any(Player.class), anyList());
        }

        @Test
        void shouldReturnEmptyForZeroCount() {
            when(playerRepository.findAll()).thenReturn(Collections.emptyList());

            List<PlayerStatsDto> result = playerService.getBestPlayers(0);

            assertThat(result).isEmpty();
            verify(playerRepository).findAll();
            verifyNoMoreInteractions(playerRepository, matchRepository, statsService);
        }

        @Test
        void shouldReturnAllWhenCountGreaterThanPlayers() {
            Player playerA = testPlayer().withId(ID_A).withUsername(USERNAME_A).build();
            Player playerB = testPlayer().withId(ID_B).withUsername(USERNAME_B).build();
            List<Player> players = List.of(playerA, playerB);

            PlayerScore playerScoreA = testPlayerScore().withId(ID_A).withScore(SCORE).build();
            PlayerScore playerScoreB = testPlayerScore().withId(ID_B).withScore(SCORE).build();

            List<MatchResult> matchesA = List.of(
                    testMatchResult().withScores(List.of(playerScoreA)).build());
            List<MatchResult> matchesB = List.of(
                    testMatchResult().withScores(List.of(playerScoreB)).build());

            PlayerStatsDto statsA = testPlayerStats().withUsername(USERNAME_A).buildDto();
            PlayerStatsDto statsB = testPlayerStats().withUsername(USERNAME_B).buildDto();

            when(playerRepository.findAll()).thenReturn(players);
            when(matchRepository.findByPlayerId(ID_A)).thenReturn(matchesA);
            when(matchRepository.findByPlayerId(ID_B)).thenReturn(matchesB);
            when(statsService.calculatePlayerStats(playerA, matchesA)).thenReturn(statsA);
            when(statsService.calculatePlayerStats(playerB, matchesB)).thenReturn(statsB);

            List<PlayerStatsDto> result = playerService.getBestPlayers(10);

            assertThat(result)
                    .hasSize(2)
                    .extracting(PlayerStatsDto::getUsername)
                    .containsExactlyInAnyOrder(USERNAME_A, USERNAME_B);

            verify(playerRepository).findAll();
            verify(matchRepository, times(2)).findByPlayerId(any());
            verify(statsService, times(2)).calculatePlayerStats(any(Player.class), anyList());
        }
    }
}
