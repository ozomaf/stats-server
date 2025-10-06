package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.ServerInfo;
import com.azatkhaliullin.dto.MatchResultDto;
import com.azatkhaliullin.dto.ServerInfoDto;
import com.azatkhaliullin.dto.ServerStatsDto;
import com.azatkhaliullin.mapper.MatchResultMapper;
import com.azatkhaliullin.mapper.ServerInfoMapper;
import com.azatkhaliullin.repository.MatchRepository;
import com.azatkhaliullin.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerService {

    private final ServerRepository serverRepository;
    private final MatchRepository matchRepository;
    private final ServerInfoMapper serverInfoMapper;
    private final MatchResultMapper matchResultMapper;

    public Optional<ServerInfoDto> getServerInfo(String endpoint) {
        log.debug("Getting server info for endpoint: {}", endpoint);
        return serverRepository.findByEndpoint(endpoint)
                .map(serverInfoMapper::toDto);
    }

    public List<ServerInfoDto> getAllServers() {
        log.debug("Getting all servers");
        return serverRepository.findAll().stream()
                .map(serverInfoMapper::toDto)
                .toList();
    }

    public List<MatchResultDto> getMatchesSince(String endpoint, Instant since) {
        log.debug("Getting matches for server {} since {}", endpoint, since);
        return matchRepository.getMatchesSince(endpoint, since).stream()
                .map(matchResultMapper::toDto)
                .toList();
    }

    public Optional<ServerStatsDto> getServerStats(String endpoint) {
        log.debug("Getting server stats for endpoint: {}", endpoint);
        return serverRepository.findByEndpoint(endpoint)
                .map(this::buildServerStats);
    }

    public ServerStatsDto buildServerStats(ServerInfo server) {
        List<MatchResult> matches = matchRepository.getAllMatchesForServer(server.getEndpoint());

        int totalMatches = matches.size();
        int totalPlayers = 0;
        int totalScore = 0;

        for (MatchResult match : matches) {
            Map<String, Integer> scores = match.getScores();
            totalPlayers += scores.size();
            totalScore += scores.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }

        int averagePlayersPerMatch = totalMatches > 0 ? totalPlayers / totalMatches : 0;
        double averageScore = totalPlayers > 0 ? (double) totalScore / totalPlayers : 0.0;

        return ServerStatsDto.builder()
                .endpoint(server.getEndpoint())
                .name(server.getName())
                .region(server.getRegion())
                .totalMatches(totalMatches)
                .averagePlayersPerMatch(averagePlayersPerMatch)
                .totalScore(totalScore)
                .averageScore(Math.round(averageScore * 100.0) / 100.0)
                .build();
    }
}
