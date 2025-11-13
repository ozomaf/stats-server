package com.azatkhaliullin.controller;

import com.azatkhaliullin.dto.JwksResponse;
import com.azatkhaliullin.service.JwtTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.azatkhaliullin.TestConstants.JWKS_PATH;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JwkController.class)
@AutoConfigureMockMvc(addFilters = false)
class JwkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @Nested
    @DisplayName("GET /jwk/.well-known/jwks.json")
    class GetJwksTests {

        @Test
        void shouldReturnJwksResponse() throws Exception {
            JwksResponse response = JwksResponse.builder()
                    .keys(java.util.List.of())
                    .build();

            when(jwtTokenService.getJwks()).thenReturn(response);

            mockMvc.perform(get(JWKS_PATH).accept(APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.keys").isArray());

            verify(jwtTokenService).getJwks();
            verifyNoMoreInteractions(jwtTokenService);
        }

        @Test
        void shouldReturn500WhenServiceFails() throws Exception {
            when(jwtTokenService.getJwks()).thenThrow(new RuntimeException());

            mockMvc.perform(get(JWKS_PATH).accept(APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(jwtTokenService).getJwks();
            verifyNoMoreInteractions(jwtTokenService);
        }
    }
}
