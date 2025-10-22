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

import static com.azatkhaliullin.TestConstants.AVERAGE_PLAYER_PER_MATCH;
import static com.azatkhaliullin.TestConstants.AVERAGE_SCORE;
import static com.azatkhaliullin.TestConstants.BEST_SCORE;
import static com.azatkhaliullin.TestConstants.DEFAULT_PLAYED_AT;
import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.ID_B;
import static com.azatkhaliullin.TestConstants.SERVER_EU_ENDPOINT;
import static com.azatkhaliullin.TestConstants.SERVER_US_ENDPOINT;
import static com.azatkhaliullin.TestConstants.TOTAL_MATCHES;
import static com.azatkhaliullin.TestConstants.UNKNOWN_SERVER;
import static com.azatkhaliullin.TestConstants.WORST_SCORE;
import static com.azatkhaliullin.builder.MatchResultTestBuilder.testMatchResult;
import static com.azatkhaliullin.builder.PlayerScoreTestBuilder.testPlayerScore;
import static com.azatkhaliullin.builder.ServerInfoBuilder.testServerInfo;
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
    class GetServerInfoTests {

        @Test
        void shouldReturnMappedDtoWhenServerExists() {
            ServerInfo serverInfo = testServerInfo().withEndpoint(SERVER_EU_ENDPOINT).build();
            when(serverRepository.findByEndpoint(SERVER_EU_ENDPOINT))
                    .thenReturn(Optional.of(serverInfo));

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
    class GetAllServersTests {

        @Test
        void shouldReturnMappedServerList() {
            List<ServerInfo> servers = List.of(
                    testServerInfo().withEndpoint(SERVER_EU_ENDPOINT).build(),
                    testServerInfo().withEndpoint(SERVER_US_ENDPOINT).build());

            when(serverRepository.findAll()).thenReturn(servers);

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
    class GetMatchesSinceTests {

        @Test
        void shouldReturnMappedMatchesAfterTimestamp() {
            List<MatchResult> matches = List.of(
                    testMatchResult().withServerEndpoint(SERVER_EU_ENDPOINT)
                            .withPlayedAt(DEFAULT_PLAYED_AT.plusSeconds(60)).build(),
                    testMatchResult().withServerEndpoint(SERVER_EU_ENDPOINT)
                            .withPlayedAt(DEFAULT_PLAYED_AT.plusSeconds(120)).build(),
                    testMatchResult().withServerEndpoint(SERVER_EU_ENDPOINT)
                            .withPlayedAt(DEFAULT_PLAYED_AT.minusSeconds(360)).build());

            when(matchRepository.findByServerSince(SERVER_EU_ENDPOINT, DEFAULT_PLAYED_AT))
                    .thenAnswer(invocation -> {
                        Instant since = invocation.getArgument(1);
                        return matches.stream()
                                .filter(m -> m.getPlayedAt().isAfter(since))
                                .toList();
                    });

            List<MatchResultDto> result = serverService.getMatchesSince(SERVER_EU_ENDPOINT, DEFAULT_PLAYED_AT);

            assertThat(result)
                    .hasSize(2)
                    .allSatisfy(m -> assertThat(m.getServerEndpoint()).isEqualTo(SERVER_EU_ENDPOINT));

            verify(matchRepository).findByServerSince(SERVER_EU_ENDPOINT, DEFAULT_PLAYED_AT);
            verifyNoMoreInteractions(matchRepository);
        }

        @Test
        void shouldReturnEmptyListWhenNoMatches() {
            when(matchRepository.findByServerSince(SERVER_US_ENDPOINT, DEFAULT_PLAYED_AT)).thenReturn(emptyList());

            List<MatchResultDto> result = serverService.getMatchesSince(SERVER_US_ENDPOINT, DEFAULT_PLAYED_AT);

            assertThat(result).isEmpty();

            verify(matchRepository).findByServerSince(SERVER_US_ENDPOINT, DEFAULT_PLAYED_AT);
            verifyNoMoreInteractions(matchRepository);
        }
    }

    @Nested
    @DisplayName("buildServerStats")
    class BuildServerStats {

        @Test
        void shouldCalculateTotalsAndAveragesCorrectly() {
            List<MatchResult> matches = List.of(
                    testMatchResult().withServerEndpoint(SERVER_EU_ENDPOINT)
                            .withScores(List.of(
                                    testPlayerScore().withId(ID_A).withScore(BEST_SCORE).build(),
                                    testPlayerScore().withId(ID_B).withScore(WORST_SCORE).build())).build(),
                    testMatchResult().withServerEndpoint(SERVER_EU_ENDPOINT)
                            .withScores(List.of(
                                    testPlayerScore().withId(ID_A).withScore(BEST_SCORE).build(),
                                    testPlayerScore().withId(ID_B).withScore(WORST_SCORE).build())).build());

            ServerInfo server = testServerInfo().withEndpoint(SERVER_EU_ENDPOINT).build();

            when(matchRepository.findByServerEndpoint(SERVER_EU_ENDPOINT)).thenReturn(matches);

            ServerStatsDto stats = serverService.buildServerStats(server);

            assertThat(stats.getEndpoint()).isEqualTo(SERVER_EU_ENDPOINT);
            assertThat(stats.getTotalMatches()).isEqualTo(TOTAL_MATCHES);
            assertThat(stats.getTotalScore()).isEqualTo(WORST_SCORE * 2 + BEST_SCORE * 2);
            assertThat(stats.getAverageScore()).isEqualTo(AVERAGE_SCORE);
            assertThat(stats.getAveragePlayersPerMatch()).isEqualTo(AVERAGE_PLAYER_PER_MATCH);

            verify(matchRepository).findByServerEndpoint(SERVER_EU_ENDPOINT);
            verifyNoMoreInteractions(matchRepository);
        }

        @Test
        void shouldReturnZerosWhenNoMatches() {
            ServerInfo server = testServerInfo().withEndpoint(SERVER_US_ENDPOINT).build();

            when(matchRepository.findByServerEndpoint(SERVER_US_ENDPOINT)).thenReturn(emptyList());

            ServerStatsDto stats = serverService.buildServerStats(server);

            assertThat(stats.getEndpoint()).isEqualTo(SERVER_US_ENDPOINT);
            assertThat(stats.getTotalMatches()).isZero();
            assertThat(stats.getTotalScore()).isZero();
            assertThat(stats.getAverageScore()).isZero();
            assertThat(stats.getAveragePlayersPerMatch()).isZero();

            verify(matchRepository).findByServerEndpoint(SERVER_US_ENDPOINT);
            verifyNoMoreInteractions(matchRepository);
        }
    }
}
