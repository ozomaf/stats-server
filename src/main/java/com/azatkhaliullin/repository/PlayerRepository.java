package com.azatkhaliullin.repository;

import com.azatkhaliullin.domain.Player;
import com.azatkhaliullin.util.GameConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PlayerRepository {

    private static final String KEY_PLAYERS_HASH = "players";
    private static final String KEY_PLAYER_STATS = "players:stats:";

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveAll(Collection<Player> players) {
        if (CollectionUtils.isEmpty(players)) {
            log.debug("No players to save");
            return;
        }
        try {
            Map<String, Player> playersMap = players.stream()
                    .collect(Collectors.toMap(Player::getUsername, p -> p));
            hashOps().putAll(KEY_PLAYERS_HASH, playersMap);
            log.debug("Successfully saved {} players", players.size());
        } catch (Exception e) {
            log.error("Failed to save players collection", e);
            throw e;
        }
    }

    public List<Player> findAll() {
        try {
            return hashOps().values(KEY_PLAYERS_HASH).stream()
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to load all players", e);
            return Collections.emptyList();
        }
    }

    public Optional<Player> findByUsername(String username) {
        try {
            Player player = hashOps().get(KEY_PLAYERS_HASH, username);
            return Optional.ofNullable(player);
        } catch (Exception e) {
            log.error("Failed to find player by username: {}", username, e);
            return Optional.empty();
        }
    }

    public List<Player> findRandom(int count) {
        try {
            List<String> randomUsernames = hashOps().randomKeys(KEY_PLAYERS_HASH, count);
            if (CollectionUtils.isEmpty(randomUsernames)) {
                return Collections.emptyList();
            }
            return hashOps().multiGet(KEY_PLAYERS_HASH, randomUsernames).stream()
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to find random players", e);
            return Collections.emptyList();
        }
    }

    public Map<Object, Object> findPlayerStats(UUID playerId) {
        String statsKey = playerStatsKey(playerId);
        return redisTemplate.opsForHash().entries(statsKey);
    }

    public Long totalPlayers() {
        Long size = hashOps().size(KEY_PLAYERS_HASH);
        return size < GameConstants.MIN_PLAYERS ? 0L : size;
    }

    public void updatePlayerStats(UUID playerId, Map<String, String> newStats) {
        String statsKey = playerStatsKey(playerId);
        redisTemplate.opsForHash().putAll(statsKey, newStats);
    }

    public boolean isEmpty() {
        try {
            Long size = hashOps().size(KEY_PLAYERS_HASH);
            return size == 0;
        } catch (Exception e) {
            log.error("Failed to check if player hash is empty", e);
            return true;
        }
    }

    private HashOperations<String, String, Player> hashOps() {
        return redisTemplate.opsForHash();
    }

    private String playerStatsKey(UUID playerId) {
        return KEY_PLAYER_STATS + playerId;
    }
}
