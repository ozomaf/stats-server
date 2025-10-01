package com.azatkhaliullin.repository;

import com.azatkhaliullin.domain.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PlayerRepository {

    private static final String KEY_PLAYERS = "players";
    private final RedisTemplate<String, Object> redisTemplate;

    public void save(Player player) {
        try {
            hashOps().put(KEY_PLAYERS, player.getUsername(), player);
            log.debug("Saved player with ID {}", player.getUsername());
        } catch (Exception e) {
            log.error("Failed to save player with ID {}", player.getUsername(), e);
            throw e;
        }
    }

    public void saveAll(Collection<Player> players) {
        try {
            if (players == null || players.isEmpty()) {
                log.debug("No players to save");
                return;
            }
            Map<String, Player> map = players.stream()
                    .collect(Collectors.toMap(Player::getUsername, p -> p));
            hashOps().putAll(KEY_PLAYERS, map);
            log.debug("Saved {} players", map.size());
        } catch (Exception e) {
            log.error("Failed to save players collection", e);
            throw e;
        }
    }

    public Optional<Player> findById(String id) {
        try {
            return Optional.ofNullable(hashOps().get(KEY_PLAYERS, id));
        } catch (Exception e) {
            log.error("Failed to find player with ID {}", id, e);
            return Optional.empty();
        }
    }

    public List<Player> findAll() {
        try {
            return hashOps().values(KEY_PLAYERS);
        } catch (Exception e) {
            log.error("Failed to retrieve all players", e);
            return Collections.emptyList();
        }
    }

    public boolean isEmpty() {
        try {
            Long size = hashOps().size(KEY_PLAYERS);
            return size == 0;
        } catch (Exception e) {
            log.error("Failed to check if player hash is empty", e);
            return true;
        }
    }

    private HashOperations<String, String, Player> hashOps() {
        return redisTemplate.opsForHash();
    }
}
