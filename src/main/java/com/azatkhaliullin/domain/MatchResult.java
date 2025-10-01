package com.azatkhaliullin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResult {
    private String id;
    private String serverEndpoint;
    private Instant playedAt;
    private List<PlayerScore> scores;
}
