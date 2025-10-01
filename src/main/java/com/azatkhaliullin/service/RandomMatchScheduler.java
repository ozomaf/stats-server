package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.Player;
import com.azatkhaliullin.domain.PlayerScore;
import com.azatkhaliullin.domain.ServerInfo;
import com.azatkhaliullin.repository.MatchRepository;
import com.azatkhaliullin.repository.PlayerRepository;
import com.azatkhaliullin.repository.ServerRepository;
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

    private static final int MIN_PLAYERS = 2;

    private final Random random = new Random();
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final ServerRepository serverRepository;

    @Scheduled(cron = "${stats.random-match.cron:*/5 * * * * *}")
    public void generateRandomMatch() {
        List<ServerInfo> servers = serverRepository.findAll();
        List<Player> players = playerRepository.findAll();
        if (servers.isEmpty() || players.isEmpty()) {
            log.warn("No servers or players available to generate match");
            return;
        }

        ServerInfo server = selectRandomServer(servers);
        List<Player> participants = selectRandomPlayers(players);
        List<PlayerScore> playerScores = generateScores(participants);

        MatchResult match = MatchResult.builder()
                .id(UUID.randomUUID().toString())
                .serverEndpoint(server.getEndpoint())
                .playedAt(Instant.now())
                .scores(playerScores)
                .build();

        matchRepository.addMatch(match);
        log.info("Generated random match on {} with {} players", server.getEndpoint(), participants.size());
    }

    private ServerInfo selectRandomServer(List<ServerInfo> servers) {
        ServerInfo server = servers.get(random.nextInt(servers.size()));
        log.debug("Selected server: {}", server.getEndpoint());
        return server;
    }

    private List<Player> selectRandomPlayers(List<Player> players) {
        int count = random.nextInt(MIN_PLAYERS, players.size() + 1);
        return random.ints(0, players.size())
                .distinct()
                .limit(count)
                .mapToObj(players::get)
                .toList();
    }

    private List<PlayerScore> generateScores(List<Player> participants) {
        return participants.stream()
                .map(p -> PlayerScore.builder()
                        .playerUsername(p.getUsername())
                        .playerDisplayName(p.getDisplayName())
                        .score(random.nextInt(101))
                        .build())
                .toList();
    }
}
