package com.azatkhaliullin.repository;

import com.azatkhaliullin.domain.MatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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

    public List<MatchResult> getAllMatches() {
        try {
            return getAllMatchKeys().stream()
                    .flatMap(key -> {
                        List<Object> matches = listOps().range(key, 0, -1);
                        return matches != null ? matches.stream() : Stream.empty();
                    })
                    .map(m -> (MatchResult) m)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to get all matches from Redis", e);
            return Collections.emptyList();
        }
    }

    public List<MatchResult> getAllMatchesForServer(String endpoint) {
        try {
            List<Object> matches = listOps().range(keyForServer(endpoint), 0, -1);
            if (matches == null || matches.isEmpty()) {
                log.debug("No matches found for server {}", endpoint);
                return Collections.emptyList();
            }
            return matches.stream()
                    .map(m -> (MatchResult) m)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to get matches for endpoint {}", endpoint, e);
            return Collections.emptyList();
        }
    }

    public List<MatchResult> getAllMatchesForPlayer(String username) {
        try {
            return getAllMatches().stream()
                    .filter(match -> match.getScores().containsKey(username))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to get matches for player {}", username, e);
            return Collections.emptyList();
        }
    }

    public List<MatchResult> getRecentMatches(int count) {
        try {
            return getAllMatches().stream()
                    .sorted(Comparator.comparing(MatchResult::getPlayedAt).reversed())
                    .limit(count)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to get recent matches globally from Redis", e);
            return Collections.emptyList();
        }
    }

    public List<MatchResult> getMatchesSince(String endpoint, Instant since) {
        try {
            return getAllMatchesForServer(endpoint).stream()
                    .filter(m -> m.getPlayedAt().isAfter(since))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to get matches from Redis for server {} since {}", endpoint, since, e);
            return Collections.emptyList();
        }
    }

    private String keyForServer(String endpoint) {
        return MATCH_KEY_PREFIX + endpoint;
    }

    private ListOperations<String, Object> listOps() {
        return redisTemplate.opsForList();
    }

    private Set<String> getAllMatchKeys() {
        Set<String> allKeys = redisTemplate.keys(MATCH_KEY_PREFIX + "*");
        if (allKeys.isEmpty()) {
            log.debug("No match keys found");
            return Collections.emptySet();
        }
        return allKeys;
    }
}
