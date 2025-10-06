package com.azatkhaliullin.repository;

import com.azatkhaliullin.domain.ServerInfo;
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
public class ServerRepository {

    private static final String KEY_SERVERS = "servers";
    private final RedisTemplate<String, Object> redisTemplate;

    public void saveAll(Collection<ServerInfo> servers) {
        try {
            if (servers == null || servers.isEmpty()) {
                log.debug("No servers to save");
                return;
            }
            Map<String, ServerInfo> map = servers.stream()
                    .collect(Collectors.toMap(ServerInfo::getEndpoint, s -> s));
            hashOps().putAll(KEY_SERVERS, map);
            log.debug("Saved {} servers", map.size());
        } catch (Exception e) {
            log.error("Failed to save servers collection", e);
            throw e;
        }
    }

    public Optional<ServerInfo> findByEndpoint(String endpoint) {
        try {
            return Optional.ofNullable(hashOps().get(KEY_SERVERS, endpoint));
        } catch (Exception e) {
            log.error("Failed to find server with endpoint {}", endpoint, e);
            return Optional.empty();
        }
    }

    public List<ServerInfo> findAll() {
        try {
            return hashOps().values(KEY_SERVERS);
        } catch (Exception e) {
            log.error("Failed to retrieve all servers", e);
            return Collections.emptyList();
        }
    }

    public boolean isEmpty() {
        try {
            Long size = hashOps().size(KEY_SERVERS);
            return size == 0;
        } catch (Exception e) {
            log.error("Failed to check if server hash is empty", e);
            return true;
        }
    }

    private HashOperations<String, String, ServerInfo> hashOps() {
        return redisTemplate.opsForHash();
    }
}
