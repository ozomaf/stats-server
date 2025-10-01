package com.azatkhaliullin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerScore {
    private String playerUsername;
    private String playerDisplayName;
    private int score;
}
