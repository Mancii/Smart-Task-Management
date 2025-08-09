package com.task.service;

import com.task.config.RateLimitConfig;
import com.task.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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

    public void resetRateLimit(String ip) {
        Cache cache = cacheManager.getCache(RateLimitConfig.REGISTRATION_ATTEMPTS_CACHE);
        if (cache != null) {
            String cacheKey = "ip:" + ip;
            cache.evict(cacheKey);
            log.info("Rate limit reset for IP: {} by admin: {}", 
                ip, getCurrentAdminUsername());
        }
    }

    public int getIpAttempts(String ip) {
        Cache cache = cacheManager.getCache(RateLimitConfig.REGISTRATION_ATTEMPTS_CACHE);
        if (cache == null) {
            log.warn("Cache not available when checking attempts for IP: {}", ip);
            return 0;
        }

        String cacheKey = "ip:" + ip;
        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);

        if (valueWrapper != null && valueWrapper.get() != null) {
            AtomicInteger attempts = (AtomicInteger) valueWrapper.get();
            int attemptCount = attempts.get();
            log.debug("Current attempts for IP {}: {}", ip, attemptCount);
            return attemptCount;
        }
        return 0;
    }
    
    public boolean isIpBlocked(String ip) {
        return getIpAttempts(ip) > RateLimitConfig.MAX_ATTEMPTS;
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
            log.warn("Could not get admin username for audit log", e);
        }
        return "system";
    }
}