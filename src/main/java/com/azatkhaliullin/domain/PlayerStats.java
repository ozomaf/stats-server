package com.azatkhaliullin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStats {
    private int totalMatches;
    private int totalScore;
    private int bestScore;
    private int worstScore;
    private double averageScore;
}
