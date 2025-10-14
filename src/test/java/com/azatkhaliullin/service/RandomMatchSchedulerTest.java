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

import static com.azatkhaliullin.TestDataFactory.PLAYER_A;
import static com.azatkhaliullin.TestDataFactory.PLAYER_B;
import static com.azatkhaliullin.TestDataFactory.SERVER_EU_ENDPOINT;
import static com.azatkhaliullin.TestDataFactory.player;
import static com.azatkhaliullin.TestDataFactory.serverInfo;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    @InjectMocks
    private RandomMatchScheduler scheduler;

    @Nested
    @DisplayName("generateRandomMatch")
    class GenerateRandomMatch {

        @Test
        void shouldCreateMatchWhenDataAvailable() {
            ServerInfo server = serverInfo(SERVER_EU_ENDPOINT);
            List<Player> players = List.of(player(PLAYER_A), player(PLAYER_B));

            when(serverRepository.findAll()).thenReturn(List.of(server));
            when(playerRepository.findAll()).thenReturn(players);

            scheduler.generateRandomMatch();

            ArgumentCaptor<MatchResult> captor = ArgumentCaptor.forClass(MatchResult.class);
            verify(matchRepository).addMatch(captor.capture());

            MatchResult saved = captor.getValue();
            assertThat(saved.getServerEndpoint()).isEqualTo(server.getEndpoint());
            assertThat(saved.getScores()).isNotEmpty();
            assertThat(saved.getScores()).hasSize(2);
        }

        @Test
        void shouldSkipWhenNoServers() {
            when(serverRepository.findAll()).thenReturn(emptyList());
            when(playerRepository.findAll()).thenReturn(List.of(player(PLAYER_A), player(PLAYER_B)));

            scheduler.generateRandomMatch();

            verify(matchRepository, never()).addMatch(any());
        }

        @Test
        void shouldSkipWhenNoDataAtAll() {
            when(serverRepository.findAll()).thenReturn(emptyList());
            when(playerRepository.findAll()).thenReturn(emptyList());

            scheduler.generateRandomMatch();

            verify(matchRepository, never()).addMatch(any());
        }
    }
}
