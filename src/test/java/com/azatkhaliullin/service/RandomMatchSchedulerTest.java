package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.Player;
import com.azatkhaliullin.domain.ServerInfo;
import com.azatkhaliullin.repository.MatchRepository;
import com.azatkhaliullin.repository.PlayerRepository;
import com.azatkhaliullin.repository.ServerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.azatkhaliullin.TestConstants.SERVER_EU_ENDPOINT;
import static com.azatkhaliullin.TestConstants.USERNAME_A;
import static com.azatkhaliullin.TestConstants.USERNAME_B;
import static com.azatkhaliullin.builder.PlayerTestBuilder.testPlayer;
import static com.azatkhaliullin.builder.ServerInfoBuilder.testServerInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RandomMatchSchedulerTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private ServerRepository serverRepository;
    @Mock
    private PlayerStatsUpdateService statsUpdateService;
    @InjectMocks
    private RandomMatchScheduler scheduler;

    @Nested
    @DisplayName("generateRandomMatch")
    class GenerateRandomMatchTests {

        @Test
        void shouldCreateMatchWhenDataAvailable() {
            ServerInfo server = testServerInfo().withEndpoint(SERVER_EU_ENDPOINT).build();
            List<Player> players = List.of(
                    testPlayer().withUsername(USERNAME_A).build(),
                    testPlayer().withUsername(USERNAME_B).build());

            when(playerRepository.totalPlayers()).thenReturn(2L);
            when(serverRepository.findRandom()).thenReturn(server);
            when(playerRepository.findRandom(anyInt())).thenReturn(players);

            scheduler.generateRandomMatch();

            ArgumentCaptor<MatchResult> captor = ArgumentCaptor.forClass(MatchResult.class);
            verify(matchRepository).save(captor.capture());

            MatchResult saved = captor.getValue();
            assertThat(saved.getServerEndpoint()).isEqualTo(server.getEndpoint());
            assertThat(saved.getScores())
                    .isNotEmpty()
                    .hasSize(2);
        }

        @Test
        void shouldSkipWhenNoServers() {
            when(serverRepository.isEmpty()).thenReturn(true);

            scheduler.generateRandomMatch();

            verify(matchRepository, never()).save(any());
        }
    }
}
