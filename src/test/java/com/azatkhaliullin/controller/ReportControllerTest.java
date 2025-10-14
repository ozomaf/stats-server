package com.azatkhaliullin.controller;

import com.azatkhaliullin.dto.MatchResultDto;
import com.azatkhaliullin.dto.PlayerStatsDto;
import com.azatkhaliullin.dto.ServerStatsDto;
import com.azatkhaliullin.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.azatkhaliullin.TestDataFactory.PLAYER_A;
import static com.azatkhaliullin.TestDataFactory.PLAYER_B;
import static com.azatkhaliullin.TestDataFactory.SERVER_EU_ENDPOINT;
import static com.azatkhaliullin.TestDataFactory.SERVER_US_ENDPOINT;
import static com.azatkhaliullin.TestDataFactory.matchResultDto;
import static com.azatkhaliullin.TestDataFactory.playerStatsDto;
import static com.azatkhaliullin.TestDataFactory.serverStatsDto;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    private static final String GET_RECENT_MATCHES_PATH = "/reports/recent-matches/{limit}";
    private static final String GET_BEST_PLAYERS_PATH = "/reports/best-players/{limit}";
    private static final String GET_POPULAR_SERVERS_PATH = "/reports/popular-servers/{limit}";
    private static final int LIMIT = 2;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Nested
    @DisplayName("GET /reports/recent-matches/{limit}")
    class GetRecentMatches {

        @Test
        void shouldReturnRecentMatches() throws Exception {
            MatchResultDto eu = matchResultDto(SERVER_EU_ENDPOINT);
            MatchResultDto us = matchResultDto(SERVER_US_ENDPOINT);
            List<MatchResultDto> matches = List.of(eu, us);

            when(reportService.getRecentMatches(LIMIT)).thenReturn(matches);

            mockMvc.perform(get(GET_RECENT_MATCHES_PATH, LIMIT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(matches.size()))
                    .andExpect(jsonPath("$[0].serverEndpoint").value(eu.getServerEndpoint()))
                    .andExpect(jsonPath("$[1].serverEndpoint").value(us.getServerEndpoint()));

            verify(reportService).getRecentMatches(LIMIT);
            verifyNoMoreInteractions(reportService);
        }

        @Test
        void shouldReturnEmptyListWhenNoMatches() throws Exception {
            when(reportService.getRecentMatches(LIMIT)).thenReturn(emptyList());

            mockMvc.perform(get(GET_RECENT_MATCHES_PATH, LIMIT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(reportService).getRecentMatches(LIMIT);
            verifyNoMoreInteractions(reportService);
        }

        @Test
        void shouldReturn500WhenServiceFails() throws Exception {
            when(reportService.getRecentMatches(LIMIT)).thenThrow(new RuntimeException("boom"));

            mockMvc.perform(get(GET_RECENT_MATCHES_PATH, LIMIT).accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(reportService).getRecentMatches(LIMIT);
            verifyNoMoreInteractions(reportService);
        }
    }

    @Nested
    @DisplayName("GET /reports/best-players/{limit}")
    class GetBestPlayers {

        @Test
        void shouldReturnBestPlayers() throws Exception {
            PlayerStatsDto a = playerStatsDto(PLAYER_A);
            PlayerStatsDto b = playerStatsDto(PLAYER_B);
            List<PlayerStatsDto> players = List.of(a, b);

            when(reportService.getBestPlayers(LIMIT)).thenReturn(players);

            mockMvc.perform(get(GET_BEST_PLAYERS_PATH, LIMIT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(players.size()))
                    .andExpect(jsonPath("$[0].username").value(PLAYER_A))
                    .andExpect(jsonPath("$[1].username").value(PLAYER_B));

            verify(reportService).getBestPlayers(LIMIT);
            verifyNoMoreInteractions(reportService);
        }

        @Test
        void shouldReturnEmptyListWhenNoPlayers() throws Exception {
            when(reportService.getBestPlayers(LIMIT)).thenReturn(emptyList());

            mockMvc.perform(get(GET_BEST_PLAYERS_PATH, LIMIT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(reportService).getBestPlayers(LIMIT);
            verifyNoMoreInteractions(reportService);
        }
    }

    @Nested
    @DisplayName("GET /reports/popular-servers/{limit}")
    class GetPopularServers {

        @Test
        void shouldReturnPopularServers() throws Exception {
            ServerStatsDto eu = serverStatsDto(SERVER_EU_ENDPOINT);
            ServerStatsDto us = serverStatsDto(SERVER_US_ENDPOINT);
            List<ServerStatsDto> servers = List.of(eu, us);

            when(reportService.getPopularServers(LIMIT)).thenReturn(servers);

            mockMvc.perform(get(GET_POPULAR_SERVERS_PATH, LIMIT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(servers.size()))
                    .andExpect(jsonPath("$[0].endpoint").value(SERVER_EU_ENDPOINT))
                    .andExpect(jsonPath("$[1].endpoint").value(SERVER_US_ENDPOINT));

            verify(reportService).getPopularServers(LIMIT);
            verifyNoMoreInteractions(reportService);
        }

        @Test
        void shouldReturnEmptyListWhenNoServers() throws Exception {
            when(reportService.getPopularServers(LIMIT)).thenReturn(emptyList());

            mockMvc.perform(get(GET_POPULAR_SERVERS_PATH, LIMIT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(reportService).getPopularServers(LIMIT);
            verifyNoMoreInteractions(reportService);
        }
    }
}
