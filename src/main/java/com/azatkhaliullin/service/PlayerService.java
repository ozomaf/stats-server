package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.dto.PlayerStatsDto;
import com.azatkhaliullin.repository.MatchRepository;
import com.azatkhaliullin.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final StatsService statsService;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    public Optional<PlayerStatsDto> getPlayerStats(String username) {
        log.debug("Getting player stats for username: {}", username);
        return playerRepository.findByUsername(username)
                .map(player -> {
                    List<MatchResult> matches = matchRepository.findByPlayerId(player.getId());
                    return statsService.calculatePlayerStats(player, matches);
                });
    }

    public List<PlayerStatsDto> getBestPlayers(int count) {
        log.debug("Getting top {} players", count);
        return playerRepository.findAll().parallelStream()
                .map(player -> {
                    List<MatchResult> matches = matchRepository.findByPlayerId(player.getId());
                    return statsService.calculatePlayerStats(player, matches);
                })
                .filter(stats -> stats.getTotalMatches() > 0)
                .sorted(Comparator.comparing(PlayerStatsDto::getRating).reversed())
                .limit(count)
                .toList();
    }
}
