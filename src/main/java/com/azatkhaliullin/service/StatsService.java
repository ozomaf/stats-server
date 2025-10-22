package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.Player;
import com.azatkhaliullin.domain.PlayerScore;
import com.azatkhaliullin.domain.PlayerStats;
import com.azatkhaliullin.dto.PlayerStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.azatkhaliullin.util.GameConstants.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    public PlayerStatsDto calculatePlayerStats(Player player, List<MatchResult> matches) {
        log.debug("Calculating stats for player: {}", player.getUsername());

        PlayerStats stats = aggregateStats(player.getId(), matches);

        return PlayerStatsDto.builder()
                .username(player.getUsername())
                .totalMatches(stats.getTotalMatches())
                .totalScore(stats.getTotalScore())
                .bestScore(stats.getBestScore())
                .worstScore(stats.getWorstScore())
                .averageScore(roundToTwoDecimals(stats.getAverageScore()))
                .rating(calculateRating(stats))
                .build();
    }

    private PlayerStats aggregateStats(UUID playerId, List<MatchResult> matches) {
        int totalMatches = 0;
        int totalScore = 0;
        int bestScore = 0;
        int worstScore = Integer.MAX_VALUE;

        for (MatchResult match : matches) {
            Optional<PlayerScore> playerScore = findPlayerScore(match, playerId);

            if (playerScore.isPresent()) {
                totalMatches++;
                int score = playerScore.get().getScore();
                totalScore += score;
                bestScore = Math.max(bestScore, score);
                worstScore = Math.min(worstScore, score);
            }
        }

        double averageScore = totalMatches > 0 ? (double) totalScore / totalMatches : DEFAULT_AVERAGE_SCORE;

        return new PlayerStats(totalMatches, totalScore, bestScore, worstScore, averageScore);
    }

    private Optional<PlayerScore> findPlayerScore(MatchResult match, UUID playerId) {
        return match.getScores().stream()
                .filter(score -> score.getPlayerId().equals(playerId))
                .findFirst();
    }


    private double calculateRating(PlayerStats stats) {
        if (stats.getTotalMatches() == 0) return DEFAULT_RATING;
        double ratingScore = WEIGHT_AVERAGE_SCORE * stats.getAverageScore()
                + WEIGHT_BEST_SCORE * stats.getBestScore()
                + WEIGHT_TOTAL_SCORE * (stats.getTotalScore() / (double) stats.getTotalMatches())
                + WEIGHT_WORST_SCORE * (WORST_SCORE_NORMALIZER / (1 + stats.getWorstScore()));
        return roundToTwoDecimals(ratingScore);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * DEFAULT_DECIMAL_SCALE) / DEFAULT_DECIMAL_SCALE;
    }
}
