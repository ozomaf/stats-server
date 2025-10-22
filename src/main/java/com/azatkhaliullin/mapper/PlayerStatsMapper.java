package com.azatkhaliullin.mapper;

import com.azatkhaliullin.domain.PlayerStats;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.azatkhaliullin.util.GameConstants.DEFAULT_AVERAGE_SCORE;

@Component
public class PlayerStatsMapper {

    public static final String TOTAL_MATCHES = "totalMatches";
    public static final String TOTAL_SCORE = "totalScore";
    public static final String BEST_SCORE = "bestScore";
    public static final String WORST_SCORE = "worstScore";
    public static final String AVERAGE_SCORE = "averageScore";

    public PlayerStats fromMap(Map<Object, Object> map) {
        int totalMatches = parseInt(map.get(TOTAL_MATCHES), 0);
        int totalScore = parseInt(map.get(TOTAL_SCORE), 0);
        int bestScore = parseInt(map.get(BEST_SCORE), 0);
        int worstScore = parseInt(map.get(WORST_SCORE), Integer.MAX_VALUE);
        double averageScore = parseDouble(map.get(AVERAGE_SCORE), DEFAULT_AVERAGE_SCORE);

        return new PlayerStats(totalMatches, totalScore, bestScore, worstScore, averageScore);
    }

    public Map<String, String> toMap(PlayerStats stats) {
        return Map.of(
                TOTAL_MATCHES, String.valueOf(stats.getTotalMatches()),
                TOTAL_SCORE, String.valueOf(stats.getTotalScore()),
                BEST_SCORE, String.valueOf(stats.getBestScore()),
                WORST_SCORE, String.valueOf(stats.getWorstScore()),
                AVERAGE_SCORE, String.valueOf(stats.getAverageScore()));
    }

    private int parseInt(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double parseDouble(Object value, double defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
