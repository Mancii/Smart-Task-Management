package com.task.service;

import com.task.repo.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenCleanupService {
    private final VerificationTokenRepository tokenRepository;
    @Value("${app.token.cleanup.retention-days:7}")
    private int tokenRetentionDays;

    // Run daily at 3 AM
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        // Keep used tokens for 7 days before deletion
        LocalDateTime cutoff = LocalDateTime.now().minusDays(tokenRetentionDays);
        int deletedCount = tokenRepository.deleteByUsedTrueAndUsedAtBefore(cutoff);

        if (deletedCount > 0) {
            log.info("Successfully cleaned up {} used verification tokens older than {}",
                    deletedCount, cutoff);
        }
    }
}