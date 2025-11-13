package com.azatkhaliullin.controller;

import com.azatkhaliullin.config.SecurityConfig;
import com.azatkhaliullin.dto.MatchResultDto;
import com.azatkhaliullin.dto.ServerInfoDto;
import com.azatkhaliullin.dto.ServerStatsDto;
import com.azatkhaliullin.security.JwtTokenFilter;
import com.azatkhaliullin.service.ServerService;
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
import java.util.Optional;

import static com.azatkhaliullin.TestConstants.DEFAULT_PLAYED_AT;
import static com.azatkhaliullin.TestConstants.DEFAULT_TIMESTAMP;
import static com.azatkhaliullin.TestConstants.GET_SERVERS_INFO_PATH;
import static com.azatkhaliullin.TestConstants.GET_SERVER_INFO_PATH;
import static com.azatkhaliullin.TestConstants.GET_SERVER_MATCHES_SINCE_PATH;
import static com.azatkhaliullin.TestConstants.GET_SERVER_STATS_PATH;
import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.ID_B;
import static com.azatkhaliullin.TestConstants.PARAM_ENDPOINT;
import static com.azatkhaliullin.TestConstants.SERVER_EU_ENDPOINT;
import static com.azatkhaliullin.TestConstants.SERVER_US_ENDPOINT;
import static com.azatkhaliullin.TestConstants.UNKNOWN_SERVER;
import static com.azatkhaliullin.builder.MatchResultTestBuilder.testMatchResult;
import static com.azatkhaliullin.builder.ServerInfoBuilder.testServerInfo;
import static com.azatkhaliullin.builder.ServerStatsDtoBuilder.testServerStatsDto;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ServerController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class)})
class ServerControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServerService serverService;

    @Nested
    @DisplayName("GET /servers/info/by-endpoint")
    class GetServerInfo {

        @Test
        void shouldReturnServerInfoWhenFound() throws Exception {
            ServerInfoDto serverInfo = testServerInfo().withEndpoint(SERVER_EU_ENDPOINT).buildDto();

            when(serverService.getServerInfo(SERVER_EU_ENDPOINT)).thenReturn(Optional.of(serverInfo));

            mockMvc.perform(get(GET_SERVER_INFO_PATH)
                            .param(PARAM_ENDPOINT, SERVER_EU_ENDPOINT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.endpoint").value(SERVER_EU_ENDPOINT));

            verify(serverService).getServerInfo(SERVER_EU_ENDPOINT);
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturn404WhenServerNotFound() throws Exception {
            when(serverService.getServerInfo(UNKNOWN_SERVER)).thenReturn(Optional.empty());

            mockMvc.perform(get(GET_SERVER_INFO_PATH)
                            .param(PARAM_ENDPOINT, UNKNOWN_SERVER).accept(APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(serverService).getServerInfo(UNKNOWN_SERVER);
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturn500WhenServiceFails() throws Exception {
            when(serverService.getServerInfo(SERVER_EU_ENDPOINT)).thenThrow(new RuntimeException());

            mockMvc.perform(get(GET_SERVER_INFO_PATH)
                            .param(PARAM_ENDPOINT, SERVER_EU_ENDPOINT).accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(serverService).getServerInfo(SERVER_EU_ENDPOINT);
            verifyNoMoreInteractions(serverService);
        }
    }

    @Nested
    @DisplayName("GET /servers/matches/{timestamp}")
    class GetMatchesSince {

        @Test
        void shouldReturnMatchesSinceTimestamp() throws Exception {
            List<MatchResultDto> matches = List.of(
                    testMatchResult().withId(ID_A).buildDto(),
                    testMatchResult().withId(ID_B).buildDto());

            when(serverService.getMatchesSince(SERVER_EU_ENDPOINT, DEFAULT_PLAYED_AT)).thenReturn(matches);

            mockMvc.perform(get(GET_SERVER_MATCHES_SINCE_PATH, DEFAULT_TIMESTAMP)
                            .param(PARAM_ENDPOINT, SERVER_EU_ENDPOINT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(matches.size()))
                    .andExpect(jsonPath("$[0].id").value(ID_A.toString()))
                    .andExpect(jsonPath("$[1].id").value(ID_B.toString()));

            verify(serverService).getMatchesSince(SERVER_EU_ENDPOINT, DEFAULT_PLAYED_AT);
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturnEmptyListWhenNoMatches() throws Exception {
            when(serverService.getMatchesSince(SERVER_US_ENDPOINT, DEFAULT_PLAYED_AT)).thenReturn(emptyList());

            mockMvc.perform(get(GET_SERVER_MATCHES_SINCE_PATH, DEFAULT_TIMESTAMP)
                            .param(PARAM_ENDPOINT, SERVER_US_ENDPOINT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(serverService).getMatchesSince(SERVER_US_ENDPOINT, DEFAULT_PLAYED_AT);
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturn500WhenServiceFails() throws Exception {
            when(serverService.getMatchesSince(SERVER_EU_ENDPOINT, DEFAULT_PLAYED_AT)).thenThrow(new RuntimeException());

            mockMvc.perform(get(GET_SERVER_MATCHES_SINCE_PATH, DEFAULT_TIMESTAMP)
                            .param(PARAM_ENDPOINT, SERVER_EU_ENDPOINT).accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(serverService).getMatchesSince(SERVER_EU_ENDPOINT, DEFAULT_PLAYED_AT);
            verifyNoMoreInteractions(serverService);
        }
    }

    @Nested
    @DisplayName("GET /servers/info")
    class GetAllServersInfo {

        @Test
        void shouldReturnAllServers() throws Exception {
            List<ServerInfoDto> servers = List.of(
                    testServerInfo().withEndpoint(SERVER_EU_ENDPOINT).buildDto(),
                    testServerInfo().withEndpoint(SERVER_US_ENDPOINT).buildDto());

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
    @DisplayName("GET /servers/stats")
    class GetServerStats {

        @Test
        void shouldReturnServerStatsWhenFound() throws Exception {
            ServerStatsDto stats = testServerStatsDto().withEndpoint(SERVER_EU_ENDPOINT).build();

            when(serverService.getServerStats(SERVER_EU_ENDPOINT)).thenReturn(Optional.of(stats));

            mockMvc.perform(get(GET_SERVER_STATS_PATH)
                            .param(PARAM_ENDPOINT, SERVER_EU_ENDPOINT).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isMap())
                    .andExpect(jsonPath("$.endpoint").value(SERVER_EU_ENDPOINT));

            verify(serverService).getServerStats(SERVER_EU_ENDPOINT);
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturn404WhenServerStatsNotFound() throws Exception {
            when(serverService.getServerStats(UNKNOWN_SERVER)).thenReturn(Optional.empty());

            mockMvc.perform(get(GET_SERVER_STATS_PATH)
                            .param(PARAM_ENDPOINT, UNKNOWN_SERVER).accept(APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(serverService).getServerStats(UNKNOWN_SERVER);
            verifyNoMoreInteractions(serverService);
        }

        @Test
        void shouldReturn500WhenServiceFails() throws Exception {
            when(serverService.getServerStats(SERVER_EU_ENDPOINT)).thenThrow(new RuntimeException());

            mockMvc.perform(get(GET_SERVER_STATS_PATH)
                            .param(PARAM_ENDPOINT, SERVER_EU_ENDPOINT).accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(serverService).getServerStats(SERVER_EU_ENDPOINT);
            verifyNoMoreInteractions(serverService);
        }
    }
}
