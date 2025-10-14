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

import java.time.Instant;
import java.util.List;

import static com.azatkhaliullin.TestDataFactory.PLAYED_AT;
import static com.azatkhaliullin.TestDataFactory.SERVER_EU_ENDPOINT;
import static com.azatkhaliullin.TestDataFactory.SERVER_GE_ENDPOINT;
import static com.azatkhaliullin.TestDataFactory.SERVER_US_ENDPOINT;
import static com.azatkhaliullin.TestDataFactory.matchResult;
import static com.azatkhaliullin.TestDataFactory.serverInfo;
import static com.azatkhaliullin.TestDataFactory.serverStats;
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
    class GetRecentMatches {

        @Test
        void shouldMapMatchesAndRespectLimit() {
            int limit = 2;
            Instant fixedTime = PLAYED_AT.toInstant();

            List<MatchResult> recentMatches = List.of(
                    matchResult(SERVER_US_ENDPOINT, fixedTime),
                    matchResult(SERVER_EU_ENDPOINT, fixedTime.plusSeconds(1800)));

            when(matchRepository.getRecentMatches(limit)).thenReturn(recentMatches);

            List<MatchResultDto> result = reportService.getRecentMatches(limit);

            assertThat(result)
                    .hasSize(2)
                    .allSatisfy(dto -> assertThat(dto.getPlayedAt()).isNotNull())
                    .extracting(MatchResultDto::getServerEndpoint)
                    .containsExactly(SERVER_US_ENDPOINT, SERVER_EU_ENDPOINT);

            verify(matchRepository).getRecentMatches(limit);
            verifyNoMoreInteractions(matchRepository);
        }


        @Test
        void shouldReturnEmptyWhenNoMatches() {
            when(matchRepository.getRecentMatches(5)).thenReturn(emptyList());

            List<MatchResultDto> result = reportService.getRecentMatches(5);

            assertThat(result).isEmpty();
            verify(matchRepository).getRecentMatches(5);
        }
    }

    @Nested
    @DisplayName("getPopularServers")
    class GetPopularServers {

        @Test
        void shouldSortServersByTotalMatchesAndRespectLimit() {
            ServerInfo eu = serverInfo(SERVER_EU_ENDPOINT);
            ServerInfo us = serverInfo(SERVER_US_ENDPOINT);
            ServerInfo ge = serverInfo(SERVER_GE_ENDPOINT);

            when(serverRepository.findAll()).thenReturn(List.of(eu, us, ge));

            ServerStatsDto e1 = serverStats(eu, 10);
            ServerStatsDto e2 = serverStats(us, 5);
            ServerStatsDto e3 = serverStats(ge, 0);

            when(serverService.buildServerStats(eu)).thenReturn(e1);
            when(serverService.buildServerStats(us)).thenReturn(e2);
            when(serverService.buildServerStats(ge)).thenReturn(e3);

            List<ServerStatsDto> result = reportService.getPopularServers(2);

            assertThat(result)
                    .hasSize(2)
                    .extracting(ServerStatsDto::getEndpoint)
                    .containsExactly(SERVER_EU_ENDPOINT, SERVER_US_ENDPOINT);

            verify(serverRepository).findAll();
            verify(serverService).buildServerStats(eu);
            verify(serverService).buildServerStats(us);
            verify(serverService).buildServerStats(ge);
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
