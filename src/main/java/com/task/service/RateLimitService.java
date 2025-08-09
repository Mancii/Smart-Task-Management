package com.task.service;

import com.task.config.RateLimitConfig;
import com.task.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final CacheManager cacheManager;

    public void checkRateLimit(String key) {
        Cache cache = cacheManager.getCache(RateLimitConfig.REGISTRATION_ATTEMPTS_CACHE);
        if (cache == null) {
            log.error("Cache not initialized");
            return;
        }

        String cacheKey = "ip:" + key;
        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);

        if (valueWrapper != null) {
            AtomicInteger attempts = (AtomicInteger) valueWrapper.get();
            if (attempts != null && attempts.incrementAndGet() > RateLimitConfig.MAX_ATTEMPTS) {
                log.warn("Rate limit exceeded for IP: {}", key);
                throw new RateLimitExceededException("Too many registration attempts. Please try again later.");
            }
        } else {
            cache.put(cacheKey, new AtomicInteger(1));
        }
    }

    public void resetRateLimit(String key) {
        Cache cache = cacheManager.getCache(RateLimitConfig.REGISTRATION_ATTEMPTS_CACHE);
        if (cache != null) {
            cache.evict("ip:" + key);
        }
    }

    public int getIpAttempts(String ip) {
        Cache cache = cacheManager.getCache(RateLimitConfig.REGISTRATION_ATTEMPTS_CACHE);
        if (cache == null) {
            return 0;
        }

        String cacheKey = "ip:" + ip;
        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);

        if (valueWrapper != null && valueWrapper.get() != null) {
            AtomicInteger attempts = (AtomicInteger) valueWrapper.get();
            assert attempts != null;
            return attempts.get() == 0 ? 0 : attempts.get();
        }
        return 0;
    }
}