package com.azatkhaliullin.repository;

import com.azatkhaliullin.domain.ServerInfo;
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
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ServerRepository {

    private static final String KEY_SERVERS_HASH = "servers";

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveAll(Collection<ServerInfo> servers) {
        if (CollectionUtils.isEmpty(servers)) {
            log.debug("No servers to save");
            return;
        }
        try {
            Map<String, ServerInfo> serversMap = servers.stream()
                    .collect(Collectors.toMap(ServerInfo::getEndpoint, s -> s));
            hashOps().putAll(KEY_SERVERS_HASH, serversMap);
            log.debug("Successfully saved {} servers", servers.size());
        } catch (Exception e) {
            log.error("Failed to save servers collection", e);
            throw e;
        }
    }

    public List<ServerInfo> findAll() {
        try {
            return hashOps().values(KEY_SERVERS_HASH).stream()
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to retrieve all servers", e);
            return Collections.emptyList();
        }
    }

    public Optional<ServerInfo> findByEndpoint(String endpoint) {
        try {
            ServerInfo serverInfo = hashOps().get(KEY_SERVERS_HASH, endpoint);
            return Optional.ofNullable(serverInfo);
        } catch (Exception e) {
            log.error("Failed to find server with endpoint {}", endpoint, e);
            return Optional.empty();
        }
    }

    public ServerInfo findRandom() {
        try {
            String endpoint = hashOps().randomKey(KEY_SERVERS_HASH);
            return findByEndpoint(endpoint)
                    .orElseThrow(() -> new IllegalStateException("Server with endpoint " + endpoint + " not found"));
        } catch (Exception e) {
            log.error("Failed to get random server", e);
            throw e;
        }
    }

    public boolean isEmpty() {
        try {
            Long size = hashOps().size(KEY_SERVERS_HASH);
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
