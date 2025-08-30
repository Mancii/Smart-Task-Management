package com.task.service;

import com.task.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
        AtomicInteger attempts = cache.get(cacheKey, () -> new AtomicInteger(1));
        
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

    public void resetRateLimit(String ip, String type) {
        Cache cache = cacheManager.getCache(AUTH_ATTEMPTS_CACHE);
        if (cache != null) {
            String cacheKey = String.format("%s:%s", type, ip);
            cache.evict(cacheKey);
            log.info("Rate limit reset for IP: {} (type: {}) by admin: {}", 
                ip, type, getCurrentAdminUsername());
        }
    }

    public Map<String, Object> getIpAttempts(String ip, String type, int maxAttempts, int windowSeconds) {
        Cache cache = cacheManager.getCache(AUTH_ATTEMPTS_CACHE);
        Map<String, Object> result = new HashMap<>();
        result.put("ip", ip);
        result.put("type", type);
        
        if (cache == null) {
            log.warn("Cache not available when checking attempts for IP: {}", ip);
            result.put("attempts", 0);
            result.put("isBlocked", false);
            return result;
        }

        String cacheKey = String.format("%s:%s", type, ip);
        int attempts = Optional.ofNullable(cache.get(cacheKey, AtomicInteger.class))
            .map(AtomicInteger::get)
            .orElse(0);
            
        result.put("attempts", attempts);
        result.put("isBlocked", attempts > maxAttempts);
        result.put("maxAttempts", maxAttempts);
        result.put("windowSeconds", windowSeconds);
        
        return result;
    }
    
    // Backward compatibility method
    public Map<String, Object> getIpAttempts(String ip, String type) {
        return getIpAttempts(ip, type, MAX_ATTEMPTS, ATTEMPT_WINDOW_SECONDS);
    }
    
    public boolean isIpBlocked(String ip, String type) {
        return getIpAttempts(ip, type).get("isBlocked").equals(true);
    }
    
    private String getCurrentAdminUsername() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal != null) {
                return principal.toString();
            }
        } catch (Exception e) {
            log.debug("Could not get admin username for audit log - using 'system'", e);
        }
        return "system";
    }
}