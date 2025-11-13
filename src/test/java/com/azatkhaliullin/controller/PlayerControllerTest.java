package com.azatkhaliullin.controller;

import com.azatkhaliullin.config.SecurityConfig;
import com.azatkhaliullin.dto.PlayerStatsDto;
import com.azatkhaliullin.security.JwtTokenFilter;
import com.azatkhaliullin.service.PlayerService;
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

import java.util.Optional;

import static com.azatkhaliullin.TestConstants.AVERAGE_SCORE;
import static com.azatkhaliullin.TestConstants.BEST_SCORE;
import static com.azatkhaliullin.TestConstants.GET_PLAYERS_USERNAME_STATS_PATH;
import static com.azatkhaliullin.TestConstants.INVALID_USERNAME;
import static com.azatkhaliullin.TestConstants.RATING;
import static com.azatkhaliullin.TestConstants.TOTAL_MATCHES;
import static com.azatkhaliullin.TestConstants.TOTAL_SCORE;
import static com.azatkhaliullin.TestConstants.USERNAME_A;
import static com.azatkhaliullin.TestConstants.WORST_SCORE;
import static com.azatkhaliullin.builder.PlayerStatsTestBuilder.testPlayerStats;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = PlayerController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtTokenFilter.class)})
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    @Nested
    @DisplayName("GET /players/{username}/stats")
    class GetPlayerStatsTests {

        @Test
        void shouldReturnPlayerStatsWhenFound() throws Exception {
            PlayerStatsDto expected = testPlayerStats().withUsername(USERNAME_A).buildDto();

            when(playerService.getPlayerStats(USERNAME_A)).thenReturn(Optional.of(expected));

            mockMvc.perform(get(GET_PLAYERS_USERNAME_STATS_PATH, USERNAME_A)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(USERNAME_A))
                    .andExpect(jsonPath("$.totalMatches").value(TOTAL_MATCHES))
                    .andExpect(jsonPath("$.totalScore").value(TOTAL_SCORE))
                    .andExpect(jsonPath("$.averageScore").value(AVERAGE_SCORE))
                    .andExpect(jsonPath("$.bestScore").value(BEST_SCORE))
                    .andExpect(jsonPath("$.worstScore").value(WORST_SCORE))
                    .andExpect(jsonPath("$.rating").value(RATING));

            verify(playerService).getPlayerStats(USERNAME_A);
            verifyNoMoreInteractions(playerService);
        }

        @Test
        void shouldReturn404WhenPlayerNotFound() throws Exception {
            when(playerService.getPlayerStats(USERNAME_A)).thenReturn(Optional.empty());

            mockMvc.perform(get(GET_PLAYERS_USERNAME_STATS_PATH, USERNAME_A)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(playerService).getPlayerStats(USERNAME_A);
            verifyNoMoreInteractions(playerService);
        }

        @Test
        void shouldReturn404ForInvalidUsername() throws Exception {
            when(playerService.getPlayerStats(INVALID_USERNAME)).thenReturn(Optional.empty());

            mockMvc.perform(get(GET_PLAYERS_USERNAME_STATS_PATH, INVALID_USERNAME)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(playerService).getPlayerStats(INVALID_USERNAME);
            verifyNoMoreInteractions(playerService);
        }

        @Test
        void shouldReturn500WhenServiceFails() throws Exception {
            when(playerService.getPlayerStats(USERNAME_A)).thenThrow(new RuntimeException());

            mockMvc.perform(get(GET_PLAYERS_USERNAME_STATS_PATH, USERNAME_A)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(playerService).getPlayerStats(USERNAME_A);
            verifyNoMoreInteractions(playerService);
        }
    }
}
