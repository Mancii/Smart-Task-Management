package com.task.repo;

import com.task.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String userName);
    boolean existsByEmail(String email);
    List<User> findByAccountNonLockedFalseAndLockTimeBefore(LocalDateTime lockTime);
}