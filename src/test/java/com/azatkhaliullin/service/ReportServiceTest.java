package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.ServerInfo;
import com.azatkhaliullin.dto.MatchResultDto;
import com.azatkhaliullin.dto.ServerStatsDto;
import com.azatkhaliullin.mapper.MatchResultMapper;
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

import java.util.List;

import static com.azatkhaliullin.TestConstants.DEFAULT_PLAYED_AT;
import static com.azatkhaliullin.TestConstants.LIMIT;
import static com.azatkhaliullin.TestConstants.SERVER_EU_ENDPOINT;
import static com.azatkhaliullin.TestConstants.SERVER_US_ENDPOINT;
import static com.azatkhaliullin.TestConstants.TOTAL_MATCHES;
import static com.azatkhaliullin.builder.MatchResultTestBuilder.testMatchResult;
import static com.azatkhaliullin.builder.ServerInfoBuilder.testServerInfo;
import static com.azatkhaliullin.builder.ServerStatsDtoBuilder.testServerStatsDto;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private ServerRepository serverRepository;
    @Mock
    private ServerService serverService;
    @InjectMocks
    private ReportService reportService;

    @Spy
    private MatchResultMapper matchResultMapper = Mappers.getMapper(MatchResultMapper.class);

    @Nested
    @DisplayName("getRecentMatches")
    class GetRecentMatchesTests {

        @Test
        void shouldMapMatchesAndRespectLimit() {
            List<MatchResult> recentMatches = List.of(
                    testMatchResult().withServerEndpoint(SERVER_US_ENDPOINT).withPlayedAt(DEFAULT_PLAYED_AT).build(),
                    testMatchResult().withServerEndpoint(SERVER_EU_ENDPOINT).withPlayedAt(DEFAULT_PLAYED_AT).build());

            when(matchRepository.findRecent(LIMIT)).thenReturn(recentMatches);

            List<MatchResultDto> result = reportService.getRecentMatches(LIMIT);

            assertThat(result)
                    .hasSize(2)
                    .allSatisfy(dto -> assertThat(dto.getPlayedAt()).isNotNull())
                    .extracting(MatchResultDto::getServerEndpoint)
                    .containsExactly(SERVER_US_ENDPOINT, SERVER_EU_ENDPOINT);

            verify(matchRepository).findRecent(LIMIT);
            verifyNoMoreInteractions(matchRepository);
        }


        @Test
        void shouldReturnEmptyWhenNoMatches() {
            when(matchRepository.findRecent(5)).thenReturn(emptyList());

            List<MatchResultDto> result = reportService.getRecentMatches(5);

            assertThat(result).isEmpty();
            verify(matchRepository).findRecent(5);
        }
    }

    @Nested
    @DisplayName("getPopularServers")
    class GetPopularServersTests {

        @Test
        void shouldSortServersByTotalMatchesAndRespectLimit() {
            ServerInfo serverInfoA = testServerInfo().withEndpoint(SERVER_EU_ENDPOINT).build();
            ServerInfo serverInfoB = testServerInfo().withEndpoint(SERVER_US_ENDPOINT).build();

            when(serverRepository.findAll()).thenReturn(List.of(serverInfoA, serverInfoB));

            ServerStatsDto serverStatsDtoA = testServerStatsDto().withEndpoint(SERVER_EU_ENDPOINT).withTotalMatches(TOTAL_MATCHES + TOTAL_MATCHES).build();
            ServerStatsDto serverStatsDtoB = testServerStatsDto().withEndpoint(SERVER_US_ENDPOINT).withTotalMatches(TOTAL_MATCHES + TOTAL_MATCHES).build();

            when(serverService.buildServerStats(serverInfoA)).thenReturn(serverStatsDtoA);
            when(serverService.buildServerStats(serverInfoB)).thenReturn(serverStatsDtoB);

            List<ServerStatsDto> result = reportService.getPopularServers(2);

            assertThat(result)
                    .hasSize(2)
                    .extracting(ServerStatsDto::getEndpoint)
                    .containsExactly(SERVER_EU_ENDPOINT, SERVER_US_ENDPOINT);

            verify(serverRepository).findAll();
            verify(serverService).buildServerStats(serverInfoA);
            verify(serverService).buildServerStats(serverInfoB);
        }

        @Test
        void shouldReturnEmptyWhenNoServers() {
            when(serverRepository.findAll()).thenReturn(emptyList());

            List<ServerStatsDto> result = reportService.getPopularServers(3);

            assertThat(result).isEmpty();
            verify(serverRepository).findAll();
            verifyNoInteractions(serverService);
        }
    }
}
