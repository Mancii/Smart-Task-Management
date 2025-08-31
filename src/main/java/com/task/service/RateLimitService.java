package com.task.service;

import com.task.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import static com.task.config.RateLimitConfig.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final CacheManager cacheManager;

    public void checkRateLimit(String ip, String type) {
        checkRateLimit(ip, type, MAX_ATTEMPTS, ATTEMPT_WINDOW_SECONDS);
    }

    public void checkRateLimit(String ip, String type, int maxAttempts, int windowSeconds) {
        Cache cache = cacheManager.getCache(AUTH_ATTEMPTS_CACHE);
        if (cache == null) {
            log.error("Rate limit cache not initialized");
            return;
        }

        // Use a simpler key since Caffeine handles TTL
        String cacheKey = String.format("%s:%s", type, ip);
        
        // Get or create the counter atomically
        AtomicInteger attempts = cache.get(cacheKey, () -> new AtomicInteger(0));
        
        if (attempts != null) {
            int currentAttempts = attempts.incrementAndGet();
            log.debug("Rate limit check - IP: {}, Type: {}, Attempts: {}/{}, Window: {}s", 
                ip, type, currentAttempts, maxAttempts, windowSeconds);
                
            if (currentAttempts > maxAttempts) {
                log.warn("Rate limit exceeded for IP: {} (type: {})", ip, type);
                throw new RateLimitExceededException(
                    String.format("Rate limit exceeded. Please try again in %d seconds", windowSeconds)
                );
            }
        }
    }
}