package com.azatkhaliullin.security;

import com.azatkhaliullin.service.JwtTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.azatkhaliullin.TestConstants.USERNAME_A;
import static com.azatkhaliullin.TestConstants.VALID_JWT_TOKEN;
import static com.azatkhaliullin.util.SecurityConstants.AUTHORIZATION_HEADER;
import static com.azatkhaliullin.util.SecurityConstants.BEARER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenFilterTest {

    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @InjectMocks
    private JwtTokenFilter jwtTokenFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("doFilterInternal")
    class DoFilterInternalTests {

        @Test
        void shouldSetAuthenticationWhenValidTokenProvided() throws Exception {
            Claims claims = Mockito.mock(Claims.class);
            when(claims.getSubject()).thenReturn(USERNAME_A);

            String bearerToken = BEARER_PREFIX + VALID_JWT_TOKEN;
            when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(bearerToken);
            when(jwtTokenService.validateAndGetClaims(VALID_JWT_TOKEN)).thenReturn(claims);

            jwtTokenFilter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(USERNAME_A);
            verify(jwtTokenService).validateAndGetClaims(VALID_JWT_TOKEN);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void shouldContinueFilterChainWhenNoTokenProvided() throws Exception {
            when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);

            jwtTokenFilter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(jwtTokenService, never()).validateAndGetClaims(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void shouldContinueFilterChainWhenHeaderDoesNotStartWithBearer() throws Exception {
            when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("Invalid " + VALID_JWT_TOKEN);

            jwtTokenFilter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(jwtTokenService, never()).validateAndGetClaims(any());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void shouldContinueFilterChainWhenTokenIsInvalid() throws Exception {
            String bearerToken = BEARER_PREFIX + VALID_JWT_TOKEN;
            when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn(bearerToken);
            when(jwtTokenService.validateAndGetClaims(VALID_JWT_TOKEN))
                    .thenThrow(new IllegalArgumentException());

            jwtTokenFilter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(jwtTokenService).validateAndGetClaims(VALID_JWT_TOKEN);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void shouldContinueFilterChainWhenEmptyHeader() throws Exception {
            when(request.getHeader(AUTHORIZATION_HEADER)).thenReturn("");

            jwtTokenFilter.doFilterInternal(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(jwtTokenService, never()).validateAndGetClaims(any());
            verify(filterChain).doFilter(request, response);
        }
    }
}

