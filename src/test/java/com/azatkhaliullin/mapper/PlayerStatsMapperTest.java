package com.azatkhaliullin.mapper;

import com.azatkhaliullin.domain.PlayerStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static com.azatkhaliullin.TestConstants.AVERAGE_SCORE;
import static com.azatkhaliullin.TestConstants.AVERAGE_SCORE_KEY;
import static com.azatkhaliullin.TestConstants.BEST_SCORE;
import static com.azatkhaliullin.TestConstants.BEST_SCORE_KEY;
import static com.azatkhaliullin.TestConstants.TOTAL_MATCHES;
import static com.azatkhaliullin.TestConstants.TOTAL_MATCHES_KEY;
import static com.azatkhaliullin.TestConstants.TOTAL_SCORE;
import static com.azatkhaliullin.TestConstants.TOTAL_SCORE_KEY;
import static com.azatkhaliullin.TestConstants.WORST_SCORE;
import static com.azatkhaliullin.TestConstants.WORST_SCORE_KEY;
import static com.azatkhaliullin.builder.PlayerStatsTestBuilder.emptyPlayerStats;
import static com.azatkhaliullin.builder.PlayerStatsTestBuilder.invalidPlayerStatsMap;
import static com.azatkhaliullin.builder.PlayerStatsTestBuilder.testPlayerStats;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PlayerStatsMapperTest {

    @InjectMocks
    private PlayerStatsMapper playerStatsMapper;

    @Nested
    @DisplayName("fromMap")
    class FromMapTests {

        @Test
        void shouldConvertMapToPlayerStats() {
            Map<Object, Object> map = testPlayerStats().buildMap();
            PlayerStats result = playerStatsMapper.fromMap(map);

            assertThat(result.getTotalMatches()).isEqualTo(TOTAL_MATCHES);
            assertThat(result.getTotalScore()).isEqualTo(TOTAL_SCORE);
            assertThat(result.getBestScore()).isEqualTo(BEST_SCORE);
            assertThat(result.getWorstScore()).isEqualTo(WORST_SCORE);
            assertThat(result.getAverageScore()).isEqualTo(AVERAGE_SCORE);
        }

        @Test
        void shouldHandleNullValues() {
            Map<Object, Object> map = emptyPlayerStats().buildMap();
            PlayerStats result = playerStatsMapper.fromMap(map);

            assertThat(result.getTotalMatches()).isZero();
            assertThat(result.getTotalScore()).isZero();
            assertThat(result.getBestScore()).isZero();
            assertThat(result.getWorstScore()).isEqualTo(Integer.MAX_VALUE);
            assertThat(result.getAverageScore()).isZero();
        }

        @Test
        void shouldHandleInvalidNumberFormats() {
            Map<Object, Object> map = invalidPlayerStatsMap();
            PlayerStats result = playerStatsMapper.fromMap(map);

            assertThat(result.getTotalMatches()).isZero();
            assertThat(result.getTotalScore()).isZero();
            assertThat(result.getBestScore()).isZero();
            assertThat(result.getWorstScore()).isEqualTo(Integer.MAX_VALUE);
            assertThat(result.getAverageScore()).isZero();
        }
    }

    @Nested
    @DisplayName("toMap")
    class ToMapTests {

        @Test
        void shouldConvertPlayerStatsToMap() {
            PlayerStats stats = testPlayerStats().buildDomain();
            Map<String, String> result = playerStatsMapper.toMap(stats);

            assertThat(result)
                    .containsEntry(TOTAL_MATCHES_KEY, String.valueOf(TOTAL_MATCHES))
                    .containsEntry(TOTAL_SCORE_KEY, String.valueOf(TOTAL_SCORE))
                    .containsEntry(BEST_SCORE_KEY, String.valueOf(BEST_SCORE))
                    .containsEntry(WORST_SCORE_KEY, String.valueOf(WORST_SCORE))
                    .containsEntry(AVERAGE_SCORE_KEY, String.valueOf(AVERAGE_SCORE));
        }

        @Test
        void shouldHandleZeroValues() {
            PlayerStats stats = emptyPlayerStats().buildDomain();
            Map<String, String> result = playerStatsMapper.toMap(stats);

            assertThat(result)
                    .containsEntry(TOTAL_MATCHES_KEY, String.valueOf(0))
                    .containsEntry(TOTAL_SCORE_KEY, String.valueOf(0))
                    .containsEntry(BEST_SCORE_KEY, String.valueOf(0))
                    .containsEntry(WORST_SCORE_KEY, String.valueOf(Integer.MAX_VALUE))
                    .containsEntry(AVERAGE_SCORE_KEY, String.valueOf(0.0));
        }
    }

    @Test
    void shouldPreserveDataWhenInverseConversion() {
        PlayerStats original = testPlayerStats().buildDomain();
        Map<String, String> map = playerStatsMapper.toMap(original);
        Map<Object, Object> objectMap = new HashMap<>(map);
        PlayerStats converted = playerStatsMapper.fromMap(objectMap);

        assertThat(converted.getTotalMatches()).isEqualTo(original.getTotalMatches());
        assertThat(converted.getTotalScore()).isEqualTo(original.getTotalScore());
        assertThat(converted.getBestScore()).isEqualTo(original.getBestScore());
        assertThat(converted.getWorstScore()).isEqualTo(original.getWorstScore());
        assertThat(converted.getAverageScore()).isEqualTo(original.getAverageScore());
    }
}
