package com.task.controller;

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
    public ResponseEntity<Void> unblockIp(
            @PathVariable String ip,
            @RequestParam(required = false, defaultValue = "*") String type) {
        
        log.info("Admin request to unblock IP: {} (type: {})", ip, type);
        
        if ("*".equals(type)) {
            // Unblock for all types
            rateLimitService.resetRateLimit(ip, "register");
            rateLimitService.resetRateLimit(ip, "login");
        } else {
            rateLimitService.resetRateLimit(ip, type);
        }
        
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ip/{ip}/attempts")
    public ResponseEntity<Map<String, Object>> getIpAttempts(
            @PathVariable String ip,
            @RequestParam(required = false, defaultValue = "*") String type) {
        
        log.debug("Admin checking attempts for IP: {} (type: {})", ip, type);
        
        if ("*".equals(type)) {
            Map<String, Object> response = new HashMap<>();
            response.put("ip", ip);
            response.put("register", rateLimitService.getIpAttempts(ip, "register"));
            response.put("login", rateLimitService.getIpAttempts(ip, "login"));
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok(rateLimitService.getIpAttempts(ip, type));
        }
    }

    @GetMapping("/ip/{ip}/status")
    public ResponseEntity<Map<String, Object>> getIpStatus(
            @PathVariable String ip,
            @RequestParam(required = false, defaultValue = "*") String type) {
        
        log.debug("Admin checking status for IP: {} (type: {})", ip, type);
        
        if ("*".equals(type)) {
            Map<String, Object> response = new HashMap<>();
            response.put("ip", ip);
            response.put("register", rateLimitService.isIpBlocked(ip, "register"));
            response.put("login", rateLimitService.isIpBlocked(ip, "login"));
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("ip", ip);
            response.put("type", type);
            response.put("isBlocked", rateLimitService.isIpBlocked(ip, type));
            response.put("message", rateLimitService.isIpBlocked(ip, type) ? 
                "This IP is currently blocked from " + type : 
                "This IP is not blocked from " + type);
            return ResponseEntity.ok(response);
        }
    }
}