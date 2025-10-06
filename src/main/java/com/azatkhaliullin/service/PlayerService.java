package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.dto.PlayerStatsDto;
import com.azatkhaliullin.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final MatchRepository matchRepository;

    public List<PlayerStatsDto> getBestPlayers(int count) {
        log.debug("Calculating top {} players", count);

        List<MatchResult> allMatches = matchRepository.getAllMatches();

        Map<String, List<MatchResult>> matchesByPlayer = allMatches.stream()
                .flatMap(match -> match.getScores().keySet().stream())
                .distinct()
                .collect(Collectors.toMap(username -> username, matchRepository::getAllMatchesForPlayer));

        return matchesByPlayer.entrySet().stream()
                .map(entry -> {
                    PlayerStatsDto stats = calculateStats(entry.getKey(), entry.getValue());
                    stats.setRating(calculateRating(stats));
                    return stats;
                })
                .sorted(Comparator.comparing(PlayerStatsDto::getRating).reversed())
                .limit(count)
                .toList();
    }

    public Optional<PlayerStatsDto> getPlayerStats(String username) {
        log.debug("Getting player stats for username: {}", username);
        List<MatchResult> matches = matchRepository.getAllMatchesForPlayer(username);
        PlayerStatsDto stats = calculateStats(username, matches);
        stats.setRating(calculateRating(stats));
        return Optional.of(stats);
    }

    private double calculateRating(PlayerStatsDto stats) {
        if (stats.getTotalMatches() == 0) return 0.0;
        double ratingScore = 0.4 * stats.getAverageScore() +
                0.3 * stats.getBestScore() +
                0.2 * (stats.getTotalScore() / (double) stats.getTotalMatches()) +
                0.1 * (100.0 / (1 + stats.getWorstScore()));
        return Math.round(ratingScore * 100.0) / 100.0;
    }

    private PlayerStatsDto calculateStats(String username, List<MatchResult> matches) {
        int totalMatches = matches.size();
        int totalScore = 0;
        int bestScore = Integer.MIN_VALUE;
        int worstScore = Integer.MAX_VALUE;

        for (MatchResult match : matches) {
            int score = match.getScores().getOrDefault(username, 0);
            totalScore += score;
            bestScore = Math.max(bestScore, score);
            worstScore = Math.min(worstScore, score);
        }

        double averageScore = totalMatches > 0 ? (double) totalScore / totalMatches : 0.0;

        return PlayerStatsDto.builder()
                .username(username)
                .totalMatches(totalMatches)
                .totalScore(totalScore)
                .averageScore(Math.round(averageScore * 100.0) / 100.0)
                .bestScore(bestScore == Integer.MIN_VALUE ? 0 : bestScore)
                .worstScore(worstScore == Integer.MAX_VALUE ? 0 : worstScore)
                .build();
    }
}
