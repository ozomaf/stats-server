package com.azatkhaliullin.controller;

import com.azatkhaliullin.config.SecurityConfig;
import com.azatkhaliullin.dto.MatchResultDto;
import com.azatkhaliullin.dto.PlayerStatsDto;
import com.azatkhaliullin.dto.ServerStatsDto;
import com.azatkhaliullin.security.JwtTokenFilter;
import com.azatkhaliullin.service.ReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.azatkhaliullin.TestConstants.GET_BEST_PLAYERS_PATH;
import static com.azatkhaliullin.TestConstants.GET_POPULAR_SERVERS_PATH;
import static com.azatkhaliullin.TestConstants.GET_RECENT_MATCHES_PATH;
import static com.azatkhaliullin.TestConstants.LIMIT;
import static com.azatkhaliullin.TestConstants.SERVER_EU_ENDPOINT;
import static com.azatkhaliullin.TestConstants.SERVER_US_ENDPOINT;
import static com.azatkhaliullin.TestConstants.USERNAME_A;
import static com.azatkhaliullin.TestConstants.USERNAME_B;
import static com.azatkhaliullin.builder.MatchResultTestBuilder.testMatchResult;
import static com.azatkhaliullin.builder.PlayerStatsTestBuilder.testPlayerStats;
import static com.azatkhaliullin.builder.ServerStatsDtoBuilder.testServerStatsDto;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ReportController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class)})
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Nested
    @DisplayName("GET /reports/recent-matches/{limit}")
    class GetRecentMatchesTests {

        @Test
        void shouldReturnRecentMatches() throws Exception {
            List<MatchResultDto> matches = List.of(
                    testMatchResult().withServerEndpoint(SERVER_EU_ENDPOINT).buildDto(),
                    testMatchResult().withServerEndpoint(SERVER_US_ENDPOINT).buildDto());

            when(reportService.getRecentMatches(LIMIT)).thenReturn(matches);

            mockMvc.perform(get(GET_RECENT_MATCHES_PATH, LIMIT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(matches.size()))
                    .andExpect(jsonPath("$[0].serverEndpoint").value(SERVER_EU_ENDPOINT))
                    .andExpect(jsonPath("$[1].serverEndpoint").value(SERVER_US_ENDPOINT));

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
            when(reportService.getRecentMatches(LIMIT)).thenThrow(new RuntimeException());

            mockMvc.perform(get(GET_RECENT_MATCHES_PATH, LIMIT).accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(reportService).getRecentMatches(LIMIT);
            verifyNoMoreInteractions(reportService);
        }
    }

    @Nested
    @DisplayName("GET /reports/best-players/{limit}")
    class GetBestPlayersTests {

        @Test
        void shouldReturnBestPlayers() throws Exception {
            List<PlayerStatsDto> players = List.of(
                    testPlayerStats().withUsername(USERNAME_A).buildDto(),
                    testPlayerStats().withUsername(USERNAME_B).buildDto());

            when(reportService.getBestPlayers(LIMIT)).thenReturn(players);

            mockMvc.perform(get(GET_BEST_PLAYERS_PATH, LIMIT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(players.size()))
                    .andExpect(jsonPath("$[0].username").value(USERNAME_A))
                    .andExpect(jsonPath("$[1].username").value(USERNAME_B));

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
    class GetPopularServersTest {

        @Test
        void shouldReturnPopularServers() throws Exception {
            List<ServerStatsDto> servers = List.of(
                    testServerStatsDto().withEndpoint(SERVER_EU_ENDPOINT).build(),
                    testServerStatsDto().withEndpoint(SERVER_US_ENDPOINT).build());

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
