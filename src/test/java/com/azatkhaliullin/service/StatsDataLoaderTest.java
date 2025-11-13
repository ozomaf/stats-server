package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.Player;
import com.azatkhaliullin.repository.PlayerRepository;
import com.azatkhaliullin.repository.ServerRepository;
import com.azatkhaliullin.repository.UserCredentialsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static com.azatkhaliullin.TestConstants.ENCODED_PASSWORD;
import static com.azatkhaliullin.TestConstants.ID_A;
import static com.azatkhaliullin.TestConstants.USERNAME_A;
import static com.azatkhaliullin.builder.PlayerTestBuilder.testPlayer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsDataLoaderTest {

    @Mock
    private ServerRepository serverRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private UserCredentialsRepository userCredentialsRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private StatsDataLoader statsDataLoader;

    @Nested
    @DisplayName("run")
    class RunTests {

        @Test
        void shouldSkipLoadingServersWhenRepositoryIsNotEmpty() {
            when(serverRepository.isEmpty()).thenReturn(false);
            when(playerRepository.isEmpty()).thenReturn(true);
            when(userCredentialsRepository.isEmpty()).thenReturn(true);
            when(playerRepository.findAll()).thenReturn(Collections.emptyList());

            statsDataLoader.run(null);

            verify(serverRepository).isEmpty();
            verify(serverRepository, never()).saveAll(anyList());
            verify(playerRepository).isEmpty();
            verify(userCredentialsRepository).isEmpty();
        }

        @Test
        void shouldSkipLoadingPlayersWhenRepositoryIsNotEmpty() {
            when(playerRepository.isEmpty()).thenReturn(false);
            when(playerRepository.findAll()).thenReturn(Collections.emptyList());
            when(serverRepository.isEmpty()).thenReturn(true);
            when(userCredentialsRepository.isEmpty()).thenReturn(true);

            statsDataLoader.run(null);

            verify(playerRepository).isEmpty();
            verify(playerRepository, never()).saveAll(anyList());
            verify(serverRepository).isEmpty();
            verify(userCredentialsRepository).isEmpty();
        }

        @Test
        void shouldSkipLoadingUserCredentialsWhenRepositoryIsNotEmpty() {
            when(userCredentialsRepository.isEmpty()).thenReturn(false);
            when(playerRepository.isEmpty()).thenReturn(false);
            when(serverRepository.isEmpty()).thenReturn(true);

            statsDataLoader.run(null);

            verify(userCredentialsRepository).isEmpty();
            verify(userCredentialsRepository, never()).saveAll(anyList());
        }

        @Test
        void shouldLoadUserCredentialsWhenRepositoryIsEmpty() {
            when(serverRepository.isEmpty()).thenReturn(true);
            when(playerRepository.isEmpty()).thenReturn(false);
            when(userCredentialsRepository.isEmpty()).thenReturn(true);

            List<Player> players = List.of(testPlayer().withId(ID_A).withUsername(USERNAME_A).build());

            when(playerRepository.findAll()).thenReturn(players);
            when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);

            statsDataLoader.run(null);

            verify(userCredentialsRepository).isEmpty();
            verify(userCredentialsRepository).saveAll(anyList());
            verify(passwordEncoder).encode(any());
        }
    }
}

