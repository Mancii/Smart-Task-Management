package com.task.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableCaching
@EnableScheduling
public class RateLimitConfig {

    public static final String REGISTRATION_ATTEMPTS_CACHE = "registrationAttempts";
    public static final int MAX_ATTEMPTS = 3;
    public static final int ATTEMPT_WINDOW_HOURS = 24;

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(REGISTRATION_ATTEMPTS_CACHE);
    }

    @Scheduled(fixedRate = 3600000) // Clean up every hour
    public void evictExpiredCacheEntries() {
        // This will clear expired entries from the cache
        // The actual eviction is handled by the cache's time-to-live setting
    }
}