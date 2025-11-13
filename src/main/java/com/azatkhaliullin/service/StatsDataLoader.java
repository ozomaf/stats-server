package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.Player;
import com.azatkhaliullin.domain.ServerInfo;
import com.azatkhaliullin.domain.UserCredentials;
import com.azatkhaliullin.repository.PlayerRepository;
import com.azatkhaliullin.repository.ServerRepository;
import com.azatkhaliullin.repository.UserCredentialsRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsDataLoader implements ApplicationRunner {

    public static final String SERVERS_JSON_PATH = "data/servers.json";
    public static final String PLAYERS_JSON_PATH = "data/players.json";
    private static final String DEFAULT_PASSWORD = "1234";

    private final ServerRepository serverRepository;
    private final PlayerRepository playerRepository;
    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        loadServers();
        loadPlayers();
        loadUsers();
    }

    private void loadServers() {
        if (!serverRepository.isEmpty()) {
            log.info("Servers already exist in Redis, skipping load");
            return;
        }

        log.info("Loading servers into Redis from JSON");
        try (InputStream is = new ClassPathResource(SERVERS_JSON_PATH).getInputStream()) {
            List<ServerInfo> servers = objectMapper.readValue(is, new TypeReference<>() {
            });
            if (CollectionUtils.isNotEmpty(servers)) {
                serverRepository.saveAll(servers);
                log.info("Loaded {} servers", servers.size());
            } else {
                log.warn("No server data found in servers.json");
            }
        } catch (Exception e) {
            log.error("Failed to load servers from JSON", e);
        }
    }

    private void loadPlayers() {
        if (!playerRepository.isEmpty()) {
            log.info("Players already exist in Redis, skipping load");
            return;
        }

        log.info("Loading players into Redis from JSON");
        try (InputStream is = new ClassPathResource(PLAYERS_JSON_PATH).getInputStream()) {
            List<Player> players = objectMapper.readValue(is, new TypeReference<>() {
            });
            if (CollectionUtils.isNotEmpty(players)) {
                playerRepository.saveAll(players);
                log.info("Loaded {} players", players.size());
            } else {
                log.warn("No player data found in players.json");
            }
        } catch (Exception e) {
            log.error("Failed to load players from JSON", e);
        }
    }

    private void loadUsers() {
        if (!userCredentialsRepository.isEmpty()) {
            log.info("User credentials already exist in Redis, skipping load");
            return;
        }

        log.info("Loading user credentials into Redis");
        try {
            List<Player> players = playerRepository.findAll();
            if (CollectionUtils.isNotEmpty(players)) {
                List<UserCredentials> userCredentials = new ArrayList<>();
                for (Player player : players) {
                    UserCredentials credentials = UserCredentials.builder()
                            .username(player.getUsername())
                            .hashedPassword(passwordEncoder.encode(DEFAULT_PASSWORD))
                            .playerId(player.getId())
                            .build();
                    userCredentials.add(credentials);
                }
                userCredentialsRepository.saveAll(userCredentials);
                log.info("Loaded {} user credentials", userCredentials.size());
            } else {
                log.warn("No players found to create user credentials");
            }
        } catch (Exception e) {
            log.error("Failed to load user credentials", e);
        }
    }
}
