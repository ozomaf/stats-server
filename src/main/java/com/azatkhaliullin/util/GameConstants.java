package com.azatkhaliullin.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GameConstants {

    public static final int MIN_PLAYERS = 2;
    public static final int MAX_SCORE = 100;

    public static final double DEFAULT_AVERAGE_SCORE = 0.0;
    public static final double DEFAULT_RATING = 0.0;
    public static final double DEFAULT_DECIMAL_SCALE = 100.0;

    public static final double WEIGHT_AVERAGE_SCORE = 0.4;
    public static final double WEIGHT_BEST_SCORE = 0.3;
    public static final double WEIGHT_TOTAL_SCORE = 0.2;
    public static final double WEIGHT_WORST_SCORE = 0.1;

    public static final double WORST_SCORE_NORMALIZER = 100.0;

}
