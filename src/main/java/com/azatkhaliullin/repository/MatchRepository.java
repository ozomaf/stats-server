package com.azatkhaliullin.repository;

import com.azatkhaliullin.domain.MatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MatchRepository {

    private static final String MATCH_KEY_PREFIX = "matches:";
    private final RedisTemplate<String, Object> redisTemplate;

    public void addMatch(MatchResult match) {
        try {
            listOps().leftPush(keyForServer(match.getServerEndpoint()), match);
            log.debug("Added match to {}: {}", match.getServerEndpoint(), match);
        } catch (Exception e) {
            log.error("Failed to add match to Redis for server {}", match.getServerEndpoint(), e);
            throw e;
        }
    }

    public List<MatchResult> getMatchesSince(String endpoint, Instant since) {
        try {
            List<Object> retrievedMatches = listOps().range(keyForServer(endpoint), 0, -1);
            if (retrievedMatches == null || retrievedMatches.isEmpty()) {
                log.debug("No matches found for server {} since {}", endpoint, since);
                return Collections.emptyList();
            }

            List<MatchResult> filtered = retrievedMatches.stream()
                    .map(m -> (MatchResult) m)
                    .filter(m -> m.getPlayedAt() != null && !m.getPlayedAt().isBefore(since))
                    .toList();

            log.debug("Retrieved {} matches for server {} since {}", filtered.size(), endpoint, since);
            return filtered;
        } catch (Exception e) {
            log.error("Failed to get matches from Redis for server {} since {}", endpoint, since, e);
            return Collections.emptyList();
        }
    }

    private ListOperations<String, Object> listOps() {
        return redisTemplate.opsForList();
    }

    private String keyForServer(String endpoint) {
        return MATCH_KEY_PREFIX + endpoint;
    }
}
