package com.azatkhaliullin.controller;

import com.azatkhaliullin.dto.PlayerStatsDto;
import com.azatkhaliullin.service.PlayerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static com.azatkhaliullin.TestDataFactory.PLAYER_A;
import static com.azatkhaliullin.TestDataFactory.PLAYER_B;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    private static final String GET_PLAYERS_USERNAME_STATS_PATH = "/players/{username}/stats";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    @Nested
    @DisplayName("GET /players/{username}/stats")
    class GetPlayerStatsTests {

        @Test
        void shouldReturnPlayerStatsWhenFound() throws Exception {
            PlayerStatsDto expected = PlayerStatsDto.builder()
                    .username(PLAYER_A)
                    .totalMatches(10)
                    .totalScore(150)
                    .averageScore(15.0)
                    .bestScore(25)
                    .worstScore(5)
                    .rating(18.5)
                    .build();

            when(playerService.getPlayerStats(PLAYER_A)).thenReturn(Optional.of(expected));

            mockMvc.perform(get(GET_PLAYERS_USERNAME_STATS_PATH, PLAYER_A)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(PLAYER_A))
                    .andExpect(jsonPath("$.totalMatches").value(10))
                    .andExpect(jsonPath("$.totalScore").value(150))
                    .andExpect(jsonPath("$.averageScore").value(15.0))
                    .andExpect(jsonPath("$.bestScore").value(25))
                    .andExpect(jsonPath("$.worstScore").value(5))
                    .andExpect(jsonPath("$.rating").value(18.5));

            verify(playerService).getPlayerStats(PLAYER_A);
            verifyNoMoreInteractions(playerService);
        }

        @Test
        void shouldReturn404WhenPlayerNotFound() throws Exception {
            when(playerService.getPlayerStats(PLAYER_B)).thenReturn(Optional.empty());

            mockMvc.perform(get(GET_PLAYERS_USERNAME_STATS_PATH, PLAYER_B)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(playerService).getPlayerStats(PLAYER_B);
            verifyNoMoreInteractions(playerService);
        }

        @Test
        void shouldReturn404ForInvalidUsername() throws Exception {
            String invalidUsername = " ";
            when(playerService.getPlayerStats(invalidUsername)).thenReturn(Optional.empty());

            mockMvc.perform(get(GET_PLAYERS_USERNAME_STATS_PATH, invalidUsername)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(playerService).getPlayerStats(invalidUsername);
            verifyNoMoreInteractions(playerService);
        }

        @Test
        void shouldReturn500WhenServiceFails() throws Exception {
            when(playerService.getPlayerStats(PLAYER_A)).thenThrow(new RuntimeException());

            mockMvc.perform(get(GET_PLAYERS_USERNAME_STATS_PATH, PLAYER_A)
                            .accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(playerService).getPlayerStats(PLAYER_A);
            verifyNoMoreInteractions(playerService);
        }
    }
}
