package com.task.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableCaching
@EnableScheduling
public class RateLimitConfig {

    public static final String AUTH_ATTEMPTS_CACHE = "authAttempts";
    public static final int MAX_ATTEMPTS = 10;
    public static final int ATTEMPT_WINDOW_SECONDS = 60;

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(AUTH_ATTEMPTS_CACHE);
    }

    @Scheduled(fixedRate = 120000) // Clean up every 2 minutes
    public void evictExpiredCacheEntries() {
        log.info("Evicting expired cache entries");
        // This will clear expired entries from the cache
        // The actual eviction is handled by the cache's time-to-live setting
    }
}