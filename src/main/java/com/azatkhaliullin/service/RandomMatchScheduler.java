package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.Player;
import com.azatkhaliullin.domain.PlayerScore;
import com.azatkhaliullin.domain.ServerInfo;
import com.azatkhaliullin.repository.MatchRepository;
import com.azatkhaliullin.repository.PlayerRepository;
import com.azatkhaliullin.repository.ServerRepository;
import com.azatkhaliullin.util.GameConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RandomMatchScheduler {

    private final Random random = new Random();
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final ServerRepository serverRepository;
    private final PlayerStatsUpdateService statsUpdateService;

    @Scheduled(cron = "${stats.random-match.cron:*/5 * * * * *}")
    public void generateRandomMatch() {
        log.debug("Starting random match generation");

        if (serverRepository.isEmpty() || playerRepository.isEmpty()) {
            log.debug("Skipping match generation cause not enough data");
            return;
        }

        Long totalPlayers = playerRepository.totalPlayers();
        int count = random.nextInt(GameConstants.MIN_PLAYERS, totalPlayers.intValue() + 1);

        ServerInfo server = serverRepository.findRandom();
        List<Player> players = playerRepository.findRandom(count);

        MatchResult match = createMatch(server, players);
        saveMatch(match);

        log.info("Generated random match {} on {} with {} players",
                match.getId(), server.getEndpoint(), players.size());
    }


    private MatchResult createMatch(ServerInfo server, List<Player> players) {
        List<PlayerScore> scores = generateScores(players);
        return MatchResult.builder()
                .id(UUID.randomUUID())
                .serverEndpoint(server.getEndpoint())
                .playedAt(Instant.now())
                .scores(scores)
                .build();
    }

    private List<PlayerScore> generateScores(List<Player> players) {
        return players.stream()
                .map(player -> PlayerScore.builder()
                        .playerId(player.getId())
                        .score(random.nextInt(GameConstants.MAX_SCORE + 1))
                        .build())
                .toList();
    }

    private void saveMatch(MatchResult match) {
        try {
            matchRepository.save(match);
            statsUpdateService.updatePlayerStats(match);
            log.debug("Successfully saved match: {}", match.getId());
        } catch (Exception e) {
            log.error("Failed to save match: {}", match.getId(), e);
            throw e;
        }
    }
}
