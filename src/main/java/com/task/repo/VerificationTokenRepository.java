package com.task.repo;

import com.task.entity.User;
import com.task.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(User user);
    void deleteByUser(User user);
    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.used = true AND t.usedAt < :cutoff")
    @Transactional
    int deleteByUsedTrueAndUsedAtBefore(LocalDateTime cutoff);
}
