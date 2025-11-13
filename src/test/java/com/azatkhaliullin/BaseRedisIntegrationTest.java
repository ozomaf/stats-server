package com.azatkhaliullin;

import com.redis.testcontainers.RedisContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class BaseRedisIntegrationTest {

    private static final String REDIS_IMAGE = "redis:7.4";
    public static final int REDIS_PORT = 6379;

    @Container
    protected static final RedisContainer REDIS_CONTAINER = new RedisContainer(REDIS_IMAGE)
            .withReuse(true)
            .withExposedPorts(REDIS_PORT);

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.url", REDIS_CONTAINER::getRedisURI);
    }
}
