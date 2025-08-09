package com.task.controller;

import com.task.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/rate-limit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRateLimitController {

    private final RateLimitService rateLimitService;

    @DeleteMapping("/ip/{ip}")
    public ResponseEntity<Void> unblockIp(@PathVariable String ip) {
        rateLimitService.resetRateLimit(ip);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ip/{ip}/attempts")
    public ResponseEntity<Integer> getIpAttempts(@PathVariable String ip) {
        int attempts = rateLimitService.getIpAttempts(ip);
        return ResponseEntity.ok(attempts);
    }
}