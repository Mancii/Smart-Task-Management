package com.task.service;

import com.task.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
        Cache cache = cacheManager.getCache(AUTH_ATTEMPTS_CACHE);
        if (cache == null) {
            log.error("Rate limit cache not initialized");
            return;
        }

        String cacheKey = String.format("%s:%s:%d", type, ip, Instant.now().getEpochSecond() / 60);
        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);

        if (valueWrapper != null) {
            AtomicInteger attempts = (AtomicInteger) valueWrapper.get();
            if (attempts != null && attempts.incrementAndGet() > MAX_ATTEMPTS) {
                log.warn("Rate limit exceeded for IP: {} (type: {})", ip, type);
                throw new RateLimitExceededException("Too many attempts. Please try again after 2 minutes");
            }
        } else {
            cache.put(cacheKey, new AtomicInteger(1));
        }
    }

    public void resetRateLimit(String ip, String type) {
        Cache cache = cacheManager.getCache(AUTH_ATTEMPTS_CACHE);
        if (cache != null) {
            // Evict all entries for this IP and type
            Object nativeCache = cache.getNativeCache();
            if (nativeCache instanceof Map) {
                ((Map<?, ?>) nativeCache).keySet().stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .filter(key -> key.startsWith(type + ":" + ip + ":"))
                    .forEach(cache::evict);
                
                log.info("Rate limit reset for IP: {} (type: {}) by admin: {}", 
                    ip, type, getCurrentAdminUsername());
            } else {
                log.warn("Cache implementation does not support key enumeration");
            }
        }
    }

    public Map<String, Object> getIpAttempts(String ip, String type) {
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

        // Count all attempts in the current minute window
        long currentMinute = Instant.now().getEpochSecond() / 60;
        String currentMinuteKey = String.format("%s:%s:%d", type, ip, currentMinute);
        
        int attempts = Optional.ofNullable(cache.get(currentMinuteKey, AtomicInteger.class))
            .map(AtomicInteger::get)
            .orElse(0);
            
        result.put("attempts", attempts);
        result.put("isBlocked", attempts > MAX_ATTEMPTS);
        result.put("maxAttempts", MAX_ATTEMPTS);
        result.put("windowSeconds", ATTEMPT_WINDOW_SECONDS);
        
        return result;
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