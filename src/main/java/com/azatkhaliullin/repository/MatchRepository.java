package com.azatkhaliullin.repository;

import com.azatkhaliullin.domain.MatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MatchRepository {

    private static final String PREFIX_MATCH = "match:";
    private static final String KEY_MATCHES_BY_TIME = "matches:by_time";
    private static final String KEY_MATCHES_BY_PLAYER = "matches:by_player:";
    private static final String KEY_MATCHES_BY_SERVER = "matches:by_server:";
    private static final String KEY_MATCHES_BY_SERVER_SINCE = "matches:by_server_since:";

    private final RedisTemplate<String, Object> redisTemplate;


    public void save(MatchResult match) {
        try {
            valueOps().set(matchKey(match.getId()), match);

            addToServerIndex(match);
            addToTimeIndex(match);
            addToPlayerIndexes(match);

            log.debug("Successfully saved match: {}", match.getId());
        } catch (Exception e) {
            log.error("Failed to save match: {}", match.getId(), e);
            throw e;
        }
    }

    public List<MatchResult> findByServerEndpoint(String endpoint) {
        try {
            Set<Object> matchIds = setOps().members(serverKey(endpoint));
            return getMatchesByIds(matchIds);
        } catch (Exception e) {
            log.error("Failed to get matches for server: {}", endpoint, e);
            return Collections.emptyList();
        }
    }

    public List<MatchResult> findByPlayerId(UUID playerId) {
        try {
            Set<Object> matchIds = setOps().members(playerKey(playerId));
            return getMatchesByIds(matchIds);
        } catch (Exception e) {
            log.error("Failed to get matches for player: {}", playerId, e);
            return Collections.emptyList();
        }
    }

    public List<MatchResult> findRecent(int count) {
        try {
            Set<Object> matchIds = zSetOps().reverseRange(KEY_MATCHES_BY_TIME, 0, count - 1L);
            return getMatchesByIds(matchIds);
        } catch (Exception e) {
            log.error("Failed to get recent matches", e);
            return Collections.emptyList();
        }
    }

    public List<MatchResult> findByServerSince(String endpoint, Instant since) {
        try {
            Set<Object> matchIds = zSetOps().rangeByScore(
                    serverTimeKey(endpoint), since.toEpochMilli(), Double.MAX_VALUE);
            return getMatchesByIds(matchIds);
        } catch (Exception e) {
            log.error("Failed to get matches for server {} since {}", endpoint, since, e);
            return Collections.emptyList();
        }
    }

    private List<MatchResult> getMatchesByIds(Set<Object> matchIds) {
        if (CollectionUtils.isEmpty(matchIds)) {
            log.debug("No matches found");
            return Collections.emptyList();
        }

        List<String> matchKeys = matchIds.stream()
                .filter(Objects::nonNull)
                .map(id -> matchKey(UUID.fromString(id.toString())))
                .toList();

        List<Object> matches = valueOps().multiGet(matchKeys);
        if (CollectionUtils.isEmpty(matches)) return Collections.emptyList();

        return matches.stream()
                .filter(Objects::nonNull)
                .filter(MatchResult.class::isInstance)
                .map(MatchResult.class::cast)
                .toList();
    }

    private void addToServerIndex(MatchResult match) {
        setOps().add(serverKey(match.getServerEndpoint()), match.getId());
        zSetOps().add(serverTimeKey(match.getServerEndpoint()),
                match.getId(), match.getPlayedAt().toEpochMilli());
    }

    private void addToTimeIndex(MatchResult match) {
        zSetOps().add(KEY_MATCHES_BY_TIME, match.getId(), match.getPlayedAt().toEpochMilli());
    }

    private void addToPlayerIndexes(MatchResult match) {
        match.getScores().forEach(playerScore ->
                setOps().add(playerKey(playerScore.getPlayerId()), match.getId()));
    }

    private SetOperations<String, Object> setOps() {
        return redisTemplate.opsForSet();
    }

    private ZSetOperations<String, Object> zSetOps() {
        return redisTemplate.opsForZSet();
    }

    private ValueOperations<String, Object> valueOps() {
        return redisTemplate.opsForValue();
    }

    private String matchKey(UUID matchId) {
        return PREFIX_MATCH + matchId;
    }

    private String serverKey(String endpoint) {
        return KEY_MATCHES_BY_SERVER + endpoint;
    }

    private String serverTimeKey(String endpoint) {
        return KEY_MATCHES_BY_SERVER_SINCE + endpoint;
    }

    private String playerKey(UUID playerId) {
        return KEY_MATCHES_BY_PLAYER + playerId;
    }
}
