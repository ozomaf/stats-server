package com.azatkhaliullin.repository;

import com.azatkhaliullin.domain.UserCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserCredentialsRepository {

    private static final String KEY_USERS_HASH = "users:credentials";

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveAll(Collection<UserCredentials> userCredentials) {
        if (CollectionUtils.isEmpty(userCredentials)) {
            log.debug("No user credentials to save");
            return;
        }
        try {
            Map<String, UserCredentials> userCredentialsMap = userCredentials.stream()
                    .collect(Collectors.toMap(UserCredentials::getUsername, u -> u));
            hashOps().putAll(KEY_USERS_HASH, userCredentialsMap);
            log.debug("Successfully saved {} user credentials", userCredentials.size());
        } catch (Exception e) {
            log.error("Failed to save user credentials collection", e);
            throw e;
        }
    }

    public Optional<UserCredentials> findByUsername(String username) {
        try {
            UserCredentials credentials = hashOps().get(KEY_USERS_HASH, username);
            return Optional.ofNullable(credentials);
        } catch (Exception e) {
            log.error("Failed to find user credentials for username: {}", username, e);
            return Optional.empty();
        }
    }

    public boolean isEmpty() {
        try {
            Long size = hashOps().size(KEY_USERS_HASH);
            return size == 0;
        } catch (Exception e) {
            log.error("Failed to check if user credentials hash is empty", e);
            return true;
        }
    }

    private HashOperations<String, String, UserCredentials> hashOps() {
        return redisTemplate.opsForHash();
    }
}
