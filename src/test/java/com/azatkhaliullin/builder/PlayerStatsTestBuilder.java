package com.azatkhaliullin.builder;

import com.azatkhaliullin.domain.PlayerStats;
import com.azatkhaliullin.dto.PlayerStatsDto;

import java.util.Map;

import static com.azatkhaliullin.TestConstants.AVERAGE_SCORE;
import static com.azatkhaliullin.TestConstants.AVERAGE_SCORE_KEY;
import static com.azatkhaliullin.TestConstants.BEST_SCORE;
import static com.azatkhaliullin.TestConstants.BEST_SCORE_KEY;
import static com.azatkhaliullin.TestConstants.RATING;
import static com.azatkhaliullin.TestConstants.TOTAL_MATCHES;
import static com.azatkhaliullin.TestConstants.TOTAL_MATCHES_KEY;
import static com.azatkhaliullin.TestConstants.TOTAL_SCORE;
import static com.azatkhaliullin.TestConstants.TOTAL_SCORE_KEY;
import static com.azatkhaliullin.TestConstants.USERNAME_A;
import static com.azatkhaliullin.TestConstants.WORST_SCORE;
import static com.azatkhaliullin.TestConstants.WORST_SCORE_KEY;

public class PlayerStatsTestBuilder {

    private String username = USERNAME_A;
    private int totalMatches = TOTAL_MATCHES;
    private int totalScore = TOTAL_SCORE;
    private int bestScore = BEST_SCORE;
    private int worstScore = WORST_SCORE;
    private double averageScore = AVERAGE_SCORE;
    private double rating = RATING;


    public static PlayerStatsTestBuilder testPlayerStats() {
        return new PlayerStatsTestBuilder();
    }

    public static PlayerStatsTestBuilder emptyPlayerStats() {
        return new PlayerStatsTestBuilder()
                .withTotalMatches(0)
                .withTotalScore(0)
                .withBestScore(0)
                .withWorstScore(Integer.MAX_VALUE)
                .withAverageScore(0.0)
                .withRating(0.0);
    }

    public static Map<Object, Object> invalidPlayerStatsMap() {
        return Map.of(
                TOTAL_MATCHES_KEY, "invalid",
                TOTAL_SCORE_KEY, "not-a-number",
                BEST_SCORE_KEY, "abc",
                WORST_SCORE_KEY, "xyz",
                AVERAGE_SCORE_KEY, "def");
    }

    public PlayerStatsTestBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public PlayerStatsTestBuilder withTotalMatches(int totalMatches) {
        this.totalMatches = totalMatches;
        return this;
    }

    public PlayerStatsTestBuilder withTotalScore(int totalScore) {
        this.totalScore = totalScore;
        return this;
    }

    public PlayerStatsTestBuilder withBestScore(int bestScore) {
        this.bestScore = bestScore;
        return this;
    }

    public PlayerStatsTestBuilder withWorstScore(int worstScore) {
        this.worstScore = worstScore;
        return this;
    }

    public PlayerStatsTestBuilder withAverageScore(double averageScore) {
        this.averageScore = averageScore;
        return this;
    }

    public PlayerStatsTestBuilder withRating(double rating) {
        this.rating = rating;
        return this;
    }

    public PlayerStatsDto buildDto() {
        return PlayerStatsDto.builder()
                .username(username)
                .totalMatches(totalMatches)
                .totalScore(totalScore)
                .bestScore(bestScore)
                .worstScore(worstScore)
                .averageScore(averageScore)
                .rating(rating)
                .build();
    }

    public PlayerStats buildDomain() {
        return new PlayerStats(totalMatches, totalScore, bestScore, worstScore, averageScore);
    }

    public Map<Object, Object> buildMap() {
        return Map.of(
                TOTAL_MATCHES_KEY, totalMatches,
                TOTAL_SCORE_KEY, totalScore,
                BEST_SCORE_KEY, bestScore,
                WORST_SCORE_KEY, worstScore,
                AVERAGE_SCORE_KEY, averageScore);
    }
}
