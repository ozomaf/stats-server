package com.azatkhaliullin.controller;

import com.azatkhaliullin.dto.MatchResultDto;
import com.azatkhaliullin.dto.ServerInfoDto;
import com.azatkhaliullin.dto.ServerStatsDto;
import com.azatkhaliullin.service.ServerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.azatkhaliullin.TestDataFactory.SERVER_EU_ENDPOINT;
import static com.azatkhaliullin.TestDataFactory.SERVER_US_ENDPOINT;
import static com.azatkhaliullin.TestDataFactory.UNKNOWN_SERVER;
import static com.azatkhaliullin.TestDataFactory.matchResultDto;
import static com.azatkhaliullin.TestDataFactory.serverInfoDto;
import static com.azatkhaliullin.TestDataFactory.serverStatsDto;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServerController.class)
class ServerControllerTest {

    public static final String GET_SERVER_INFO_PATH = "/servers/{endpoint}/info";
    public static final String GET_SERVER_MATCHES_SINCE_PATH = "/servers/{endpoint}/matches/{timestamp}";
    public static final String GET_SERVERS_INFO_PATH = "/servers/info";
    public static final String GET_SERVER_STATS_PATH = "/servers/{endpoint}/stats";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServerService serverService;

    @Nested
    @DisplayName("GET /servers/{endpoint}/info")
    class GetServerInfo {

        @Test
        void shouldReturnServerInfoWhenFound() throws Exception {
            ServerInfoDto serverInfo = serverInfoDto(SERVER_EU_ENDPOINT);

            when(serverService.getServerInfo(SERVER_EU_ENDPOINT)).thenReturn(Optional.of(serverInfo));

            mockMvc.perform(get(GET_SERVER_INFO_PATH, SERVER_EU_ENDPOINT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.endpoint").value(SERVER_EU_ENDPOINT));

            verify(serverService).getServerInfo(SERVER_EU_ENDPOINT);
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturn404WhenServerNotFound() throws Exception {
            when(serverService.getServerInfo(UNKNOWN_SERVER)).thenReturn(Optional.empty());

            mockMvc.perform(get(GET_SERVER_INFO_PATH, UNKNOWN_SERVER).accept(APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(serverService).getServerInfo(UNKNOWN_SERVER);
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturn500WhenServiceFails() throws Exception {
            when(serverService.getServerInfo(SERVER_EU_ENDPOINT)).thenThrow(new RuntimeException());

            mockMvc.perform(get(GET_SERVER_INFO_PATH, SERVER_EU_ENDPOINT).accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(serverService).getServerInfo(SERVER_EU_ENDPOINT);
            verifyNoMoreInteractions(serverService);
        }
    }

    @Nested
    @DisplayName("GET /servers/{endpoint}/matches/{timestamp}")
    class GetMatchesSince {

        @Test
        void shouldReturnMatchesSinceTimestamp() throws Exception {
            long timestamp = 1_700_000_000L;
            Instant since = Instant.ofEpochSecond(timestamp);

            MatchResultDto m1 = matchResultDto(SERVER_EU_ENDPOINT);
            MatchResultDto m2 = matchResultDto(SERVER_EU_ENDPOINT);
            List<MatchResultDto> matches = List.of(m1, m2);

            when(serverService.getMatchesSince(SERVER_EU_ENDPOINT, since)).thenReturn(matches);

            mockMvc.perform(get(GET_SERVER_MATCHES_SINCE_PATH, SERVER_EU_ENDPOINT, timestamp)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(matches.size()))
                    .andExpect(jsonPath("$[0].id").value(m1.getId()))
                    .andExpect(jsonPath("$[1].id").value(m2.getId()));

            verify(serverService).getMatchesSince(SERVER_EU_ENDPOINT, since);
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturnEmptyListWhenNoMatches() throws Exception {
            long timestamp = 1_700_000_000L;
            Instant since = Instant.ofEpochSecond(timestamp);

            when(serverService.getMatchesSince(SERVER_US_ENDPOINT, since)).thenReturn(emptyList());

            mockMvc.perform(get(GET_SERVER_MATCHES_SINCE_PATH, SERVER_US_ENDPOINT, timestamp)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(serverService).getMatchesSince(SERVER_US_ENDPOINT, since);
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturn500WhenServiceFails() throws Exception {
            long timestamp = 1_700_000_000L;
            Instant since = Instant.ofEpochSecond(timestamp);

            when(serverService.getMatchesSince(SERVER_EU_ENDPOINT, since)).thenThrow(new RuntimeException());

            mockMvc.perform(get(GET_SERVER_MATCHES_SINCE_PATH, SERVER_EU_ENDPOINT, timestamp)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(serverService).getMatchesSince(SERVER_EU_ENDPOINT, since);
            verifyNoMoreInteractions(serverService);
        }
    }

    @Nested
    @DisplayName("GET /servers/info")
    class GetAllServersInfo {

        @Test
        void shouldReturnAllServers() throws Exception {
            List<ServerInfoDto> servers = List.of(
                    serverInfoDto(SERVER_EU_ENDPOINT),
                    serverInfoDto(SERVER_US_ENDPOINT));

            when(serverService.getAllServers()).thenReturn(servers);

            mockMvc.perform(get(GET_SERVERS_INFO_PATH).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(servers.size()))
                    .andExpect(jsonPath("$[0].endpoint").value(SERVER_EU_ENDPOINT))
                    .andExpect(jsonPath("$[1].endpoint").value(SERVER_US_ENDPOINT));

            verify(serverService).getAllServers();
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturnEmptyListWhenNoServers() throws Exception {
            when(serverService.getAllServers()).thenReturn(emptyList());

            mockMvc.perform(get(GET_SERVERS_INFO_PATH).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(serverService).getAllServers();
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturn500WhenServiceFails() throws Exception {
            when(serverService.getAllServers()).thenThrow(new RuntimeException());

            mockMvc.perform(get(GET_SERVERS_INFO_PATH).accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(serverService).getAllServers();
            verifyNoMoreInteractions(serverService);
        }
    }

    @Nested
    @DisplayName("GET /servers/{endpoint}/stats")
    class GetServerStats {

        @Test
        void shouldReturnServerStatsWhenFound() throws Exception {
            ServerStatsDto stats = serverStatsDto(SERVER_EU_ENDPOINT);

            when(serverService.getServerStats(SERVER_EU_ENDPOINT)).thenReturn(Optional.of(stats));

            mockMvc.perform(get(GET_SERVER_STATS_PATH, SERVER_EU_ENDPOINT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isMap())
                    .andExpect(jsonPath("$.endpoint").value(SERVER_EU_ENDPOINT));

            verify(serverService).getServerStats(SERVER_EU_ENDPOINT);
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturn404WhenServerStatsNotFound() throws Exception {
            when(serverService.getServerStats(UNKNOWN_SERVER)).thenReturn(Optional.empty());

            mockMvc.perform(get(GET_SERVER_STATS_PATH, UNKNOWN_SERVER).accept(APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(serverService).getServerStats(UNKNOWN_SERVER);
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturn500WhenServiceFails() throws Exception {
            when(serverService.getServerStats(SERVER_EU_ENDPOINT)).thenThrow(new RuntimeException("boom"));

            mockMvc.perform(get(GET_SERVER_STATS_PATH, SERVER_EU_ENDPOINT).accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(serverService).getServerStats(SERVER_EU_ENDPOINT);
            verifyNoMoreInteractions(serverService);
        }
    }
}
