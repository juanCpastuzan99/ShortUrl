package com.parcial.urlshortener.infrastructure.adapters.output.redis;

import com.parcial.urlshortener.domain.ports.output.LinkCachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisLinkCacheAdapter implements LinkCachePort {

    private static final Logger log = LoggerFactory.getLogger(RedisLinkCacheAdapter.class);
    private static final String KEY_PREFIX = "link:";
    private static final Duration CACHE_TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    @Value("${app.redis-cache-min-length:50}")
    private int minLength;

    public RedisLinkCacheAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void cacheLink(String shortCode, String originalUrl) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + shortCode, originalUrl, CACHE_TTL);
        } catch (RedisConnectionFailureException ex) {
            log.warn("[Redis] No disponible — operación cacheLink omitida para {}", shortCode);
        }
    }

    @Override
    public Optional<String> getCachedLink(String shortCode) {
        try {
            String value = redisTemplate.opsForValue().get(KEY_PREFIX + shortCode);
            return Optional.ofNullable(value);
        } catch (RedisConnectionFailureException ex) {
            log.warn("[Redis] No disponible — getCachedLink omitido para {}", shortCode);
            return Optional.empty();
        }
    }

    @Override
    public boolean isCacheable(String originalUrl) {
        return originalUrl != null && originalUrl.length() >= minLength;
    }
}
