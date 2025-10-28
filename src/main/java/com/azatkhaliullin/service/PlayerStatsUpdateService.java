package com.azatkhaliullin.service;

import com.azatkhaliullin.domain.MatchResult;
import com.azatkhaliullin.domain.PlayerScore;
import com.azatkhaliullin.domain.PlayerStats;
import com.azatkhaliullin.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.azatkhaliullin.service.StatsService.roundToTwoDecimals;
import static com.azatkhaliullin.util.GameConstants.DEFAULT_AVERAGE_SCORE;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerStatsUpdateService {

    private final PlayerRepository playerRepository;

    public void updatePlayerStats(MatchResult match) {
        match.getScores().forEach(this::updateSinglePlayerStats);
        log.debug("Successfully updated player stats for match: {}", match.getId());
    }

    private void updateSinglePlayerStats(PlayerScore score) {
        UUID playerId = score.getPlayerId();
        try {
            PlayerStats currentStats = playerRepository.findPlayerStats(playerId);
            PlayerStats updatedStats = mergeStats(currentStats, score);

            playerRepository.updatePlayerStats(playerId, updatedStats);

            log.debug("Updated stats for player {}", playerId);
        } catch (Exception e) {
            log.error("Failed to update stats for player {}", playerId, e);
            throw e;
        }
    }

    private PlayerStats mergeStats(PlayerStats current, PlayerScore newScore) {
        int totalMatches = current.getTotalMatches() + 1;
        int totalScore = current.getTotalScore() + newScore.getScore();
        int bestScore = Math.max(current.getBestScore(), newScore.getScore());
        int worstScore = Math.min(current.getWorstScore(), newScore.getScore());
        double averageScore = roundToTwoDecimals(
                totalMatches > 0 ? (double) totalScore / totalMatches : DEFAULT_AVERAGE_SCORE);

        return new PlayerStats(totalMatches, totalScore, bestScore, worstScore, averageScore);
    }
}
