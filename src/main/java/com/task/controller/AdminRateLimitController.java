package com.task.controller;

import com.task.config.RateLimitConfig;
import com.task.service.RateLimitService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/rate-limit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "IP Rate Limit Management", description = "Endpoints for managing IP-based rate limiting")
@Slf4j
public class AdminRateLimitController {

    private final RateLimitService rateLimitService;

    @DeleteMapping("/ip/{ip}")
    public ResponseEntity<Void> unblockIp(@PathVariable String ip) {
        log.info("Admin request to unblock IP: {}", ip);
        rateLimitService.resetRateLimit(ip);
        log.info("Successfully unblocked IP: {}", ip);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ip/{ip}/attempts")
    public ResponseEntity<Map<String, Object>> getIpAttempts(@PathVariable String ip) {
        log.debug("Admin checking attempts for IP: {}", ip);
        int attempts = rateLimitService.getIpAttempts(ip);
        boolean isBlocked = rateLimitService.isIpBlocked(ip);
        
        Map<String, Object> response = new HashMap<>();
        response.put("ip", ip);
        response.put("attempts", attempts);
        response.put("isBlocked", isBlocked);
        response.put("maxAttempts", RateLimitConfig.MAX_ATTEMPTS);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ip/{ip}/status")
    public ResponseEntity<Map<String, Object>> getIpStatus(@PathVariable String ip) {
        log.debug("Admin checking status for IP: {}", ip);
        boolean isBlocked = rateLimitService.isIpBlocked(ip);
        
        Map<String, Object> response = new HashMap<>();
        response.put("ip", ip);
        response.put("isBlocked", isBlocked);
        response.put("message", isBlocked ? 
            "This IP is currently blocked from registration" : 
            "This IP is not blocked");
            
        return ResponseEntity.ok(response);
    }
}