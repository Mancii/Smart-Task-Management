package com.task.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for rate limiting using Caffeine cache with TTL support.
 */
@Slf4j
@Configuration
@EnableCaching
@EnableScheduling
public class RateLimitConfig {

    public static final String AUTH_ATTEMPTS_CACHE = "authAttempts";
    public static final int MAX_ATTEMPTS = 10;
    public static final int ATTEMPT_WINDOW_SECONDS = 60;

    /**
     * Creates a Caffeine cache manager with TTL support for rate limiting.
     *
     * @return the cache manager instance
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(AUTH_ATTEMPTS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(ATTEMPT_WINDOW_SECONDS, TimeUnit.SECONDS)
            .maximumSize(10_000)
            .recordStats());
        return cacheManager;
    }

    /**
     * Logs cache statistics every minute.
     */
    @Scheduled(fixedRate = 60000) // Log cache stats every minute
    public void logCacheStats() {
        Cache cache = cacheManager().getCache(AUTH_ATTEMPTS_CACHE);
        if (cache != null && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
            com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache = 
                (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache.getNativeCache();
            log.debug("Rate limit cache stats: {}", caffeineCache.stats());
        }
    }
}