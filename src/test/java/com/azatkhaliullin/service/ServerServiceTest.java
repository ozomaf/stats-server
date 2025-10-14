package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.ServerInfo;
import com.azatkhaliullin.dto.MatchResultDto;
import com.azatkhaliullin.dto.ServerInfoDto;
import com.azatkhaliullin.dto.ServerStatsDto;
import com.azatkhaliullin.mapper.MatchResultMapper;
import com.azatkhaliullin.mapper.ServerInfoMapper;
import com.azatkhaliullin.repository.MatchRepository;
import com.azatkhaliullin.repository.ServerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.azatkhaliullin.TestDataFactory.PLAYER_A;
import static com.azatkhaliullin.TestDataFactory.SERVER_EU_ENDPOINT;
import static com.azatkhaliullin.TestDataFactory.SERVER_US_ENDPOINT;
import static com.azatkhaliullin.TestDataFactory.UNKNOWN_SERVER;
import static com.azatkhaliullin.TestDataFactory.matchResult;
import static com.azatkhaliullin.TestDataFactory.matchesForPlayer;
import static com.azatkhaliullin.TestDataFactory.serverInfo;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerServiceTest {

    @Mock
    private ServerRepository serverRepository;
    @Mock
    private MatchRepository matchRepository;
    @InjectMocks
    private ServerService serverService;

    @Spy
    private ServerInfoMapper serverInfoMapper = Mappers.getMapper(ServerInfoMapper.class);
    @Spy
    private MatchResultMapper matchResultMapper = Mappers.getMapper(MatchResultMapper.class);

    @Nested
    @DisplayName("getServerInfo")
    class GetServerInfo {

        @Test
        void shouldReturnMappedDtoWhenServerExists() {
            when(serverRepository.findByEndpoint(SERVER_EU_ENDPOINT))
                    .thenReturn(Optional.of(serverInfo(SERVER_EU_ENDPOINT)));

            Optional<ServerInfoDto> result = serverService.getServerInfo(SERVER_EU_ENDPOINT);

            assertThat(result).isPresent();
            assertThat(result.get().getEndpoint()).isEqualTo(SERVER_EU_ENDPOINT);

            verify(serverRepository).findByEndpoint(SERVER_EU_ENDPOINT);
            verifyNoMoreInteractions(serverRepository);
        }

        @Test
        void shouldReturnEmptyWhenServerNotFound() {
            when(serverRepository.findByEndpoint(UNKNOWN_SERVER)).thenReturn(Optional.empty());

            Optional<ServerInfoDto> result = serverService.getServerInfo(UNKNOWN_SERVER);

            assertThat(result).isEmpty();

            verify(serverRepository).findByEndpoint(UNKNOWN_SERVER);
            verifyNoMoreInteractions(serverRepository);
        }
    }

    @Nested
    @DisplayName("getAllServers")
    class GetAllServers {

        @Test
        void shouldReturnMappedServerList() {
            when(serverRepository.findAll()).thenReturn(List.of(
                    serverInfo(SERVER_EU_ENDPOINT),
                    serverInfo(SERVER_US_ENDPOINT)));

            List<ServerInfoDto> result = serverService.getAllServers();

            assertThat(result)
                    .hasSize(2)
                    .extracting(ServerInfoDto::getEndpoint)
                    .containsExactlyInAnyOrder(SERVER_EU_ENDPOINT, SERVER_US_ENDPOINT);

            verify(serverRepository).findAll();
            verifyNoMoreInteractions(serverRepository);
        }


        @Test
        void shouldReturnEmptyListWhenNoServers() {
            when(serverRepository.findAll()).thenReturn(emptyList());

            List<ServerInfoDto> result = serverService.getAllServers();

            assertThat(result).isEmpty();

            verify(serverRepository).findAll();
            verifyNoMoreInteractions(serverRepository);
        }
    }

    @Nested
    @DisplayName("getMatchesSince")
    class GetMatchesSince {

        @Test
        void shouldReturnMappedMatchesAfterTimestamp() {
            Instant now = Instant.now();
            List<MatchResult> matchResults = List.of(
                    matchResult(SERVER_EU_ENDPOINT, now.plusSeconds(60)),
                    matchResult(SERVER_EU_ENDPOINT, now.plusSeconds(120)),
                    matchResult(SERVER_EU_ENDPOINT, now.minusSeconds(360)));

            when(matchRepository.getMatchesSince(SERVER_EU_ENDPOINT, now))
                    .thenAnswer(invocation -> {
                        Instant since = invocation.getArgument(1);
                        return matchResults.stream()
                                .filter(m -> m.getPlayedAt().isAfter(since))
                                .toList();
                    });

            List<MatchResultDto> result = serverService.getMatchesSince(SERVER_EU_ENDPOINT, now);

            assertThat(result)
                    .hasSize(2)
                    .allSatisfy(dto -> assertThat(dto.getServerEndpoint()).isEqualTo(SERVER_EU_ENDPOINT));

            verify(matchRepository).getMatchesSince(SERVER_EU_ENDPOINT, now);
            verifyNoMoreInteractions(matchRepository);
        }

        @Test
        void shouldReturnEmptyListWhenNoMatches() {
            Instant now = Instant.now();

            when(matchRepository.getMatchesSince(SERVER_US_ENDPOINT, now)).thenReturn(emptyList());

            List<MatchResultDto> result = serverService.getMatchesSince(SERVER_US_ENDPOINT, now);

            assertThat(result).isEmpty();

            verify(matchRepository).getMatchesSince(SERVER_US_ENDPOINT, now);
            verifyNoMoreInteractions(matchRepository);
        }
    }

    @Nested
    @DisplayName("buildServerStats")
    class BuildServerStats {

        @Test
        void shouldCalculateTotalsAndAveragesCorrectly() {
            List<MatchResult> matches = matchesForPlayer(PLAYER_A, 10, 20);
            when(matchRepository.getAllMatchesForServer(SERVER_EU_ENDPOINT)).thenReturn(matches);

            ServerInfo server = serverInfo(SERVER_EU_ENDPOINT);
            ServerStatsDto stats = serverService.buildServerStats(server);

            assertThat(stats.getEndpoint()).isEqualTo(SERVER_EU_ENDPOINT);
            assertThat(stats.getTotalMatches()).isEqualTo(2);
            assertThat(stats.getTotalScore()).isEqualTo(30);
            assertThat(stats.getAverageScore()).isEqualTo(15.0);
            assertThat(stats.getAveragePlayersPerMatch()).isEqualTo(1);

            verify(matchRepository).getAllMatchesForServer(SERVER_EU_ENDPOINT);
            verifyNoMoreInteractions(matchRepository);
        }

        @Test
        void shouldReturnZerosWhenNoMatches() {
            when(matchRepository.getAllMatchesForServer(SERVER_US_ENDPOINT)).thenReturn(emptyList());

            ServerInfo server = serverInfo(SERVER_US_ENDPOINT);
            ServerStatsDto stats = serverService.buildServerStats(server);

            assertThat(stats.getEndpoint()).isEqualTo(SERVER_US_ENDPOINT);
            assertThat(stats.getTotalMatches()).isZero();
            assertThat(stats.getTotalScore()).isZero();
            assertThat(stats.getAverageScore()).isZero();
            assertThat(stats.getAveragePlayersPerMatch()).isZero();

            verify(matchRepository).getAllMatchesForServer(SERVER_US_ENDPOINT);
            verifyNoMoreInteractions(matchRepository);
        }
    }
}
