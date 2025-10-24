package com.task.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEmail("test@example.com");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
    }

    @Test
    void incrementFailedAttempts_ShouldIncrementCounter() {
        // Given
        assertEquals(0, user.getFailedLoginAttempts());
        assertTrue(user.isAccountNonLocked());

        // When
        user.incrementFailedAttempts();

        // Then
        assertEquals(1, user.getFailedLoginAttempts());
        assertTrue(user.isAccountNonLocked()); // Should still be unlocked
        assertNull(user.getLockTime());
    }

    @Test
    void incrementFailedAttempts_ShouldLockAccountAfterFiveAttempts() {
        // Given
        user.setFailedLoginAttempts(4); // Already at 4 attempts

        // When
        user.incrementFailedAttempts();

        // Then
        assertEquals(5, user.getFailedLoginAttempts());
        assertFalse(user.isAccountNonLocked()); // Should be locked now
        assertNotNull(user.getLockTime());
        assertTrue(user.getLockTime().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void incrementFailedAttempts_ShouldContinueIncrementingAfterLock() {
        // Given
        user.setFailedLoginAttempts(5);
        user.setAccountNonLocked(false);

        // When
        user.incrementFailedAttempts();

        // Then
        assertEquals(6, user.getFailedLoginAttempts());
        assertFalse(user.isAccountNonLocked());
    }

    @Test
    void resetFailedAttempts_ShouldResetCounterAndUnlockAccount() {
        // Given
        user.setFailedLoginAttempts(5);
        user.setAccountNonLocked(false);
        user.setLockTime(LocalDateTime.now());

        // When
        user.resetFailedAttempts();

        // Then
        assertEquals(0, user.getFailedLoginAttempts());
        assertTrue(user.isAccountNonLocked());
        assertNull(user.getLockTime());
    }

    @Test
    void updateLastLoginTime_ShouldSetCurrentTime() {
        // Given
        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        assertNull(user.getLastLoginTime());

        // When
        user.updateLastLoginTime();

        // Then
        assertNotNull(user.getLastLoginTime());
        assertTrue(user.getLastLoginTime().isAfter(beforeUpdate));
        assertTrue(user.getLastLoginTime().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void getAuthorities_ShouldReturnCorrectAuthority_ForUserRole() {
        // Given
        user.setRole(UserRole.USER);

        // When
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void getAuthorities_ShouldReturnCorrectAuthority_ForAdminRole() {
        // Given
        user.setRole(UserRole.ADMIN);

        // When
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void getPassword_ShouldReturnPassword() {
        // When
        String password = user.getPassword();

        // Then
        assertEquals("encodedPassword", password);
    }

    @Test
    void getUsername_ShouldReturnUsername() {
        // When
        String username = user.getUsername();

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void isAccountNonExpired_ShouldAlwaysReturnTrue() {
        // When & Then
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_ShouldReturnTrue_WhenAccountNotLocked() {
        // Given
        user.setAccountNonLocked(true);

        // When & Then
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void isAccountNonLocked_ShouldReturnFalse_WhenAccountLocked() {
        // Given
        user.setAccountNonLocked(false);
        user.setLockTime(LocalDateTime.now());

        // When & Then
        assertFalse(user.isAccountNonLocked());
    }

    @Test
    void isAccountNonLocked_ShouldAutoUnlock_WhenLockTimeExpired() {
        // Given
        user.setAccountNonLocked(false);
        user.setFailedLoginAttempts(5);
        user.setLockTime(LocalDateTime.now().minusHours(2)); // Locked 2 hours ago

        // When
        boolean result = user.isAccountNonLocked();

        // Then
        assertTrue(result); // Should auto-unlock
        assertEquals(0, user.getFailedLoginAttempts()); // Should reset attempts
        assertTrue(user.isAccountNonLocked()); // Should update field
        assertNull(user.getLockTime()); // Should clear lock time
    }

    @Test
    void isAccountNonLocked_ShouldRemainLocked_WhenLockTimeNotExpired() {
        // Given
        user.setAccountNonLocked(false);
        user.setFailedLoginAttempts(5);
        user.setLockTime(LocalDateTime.now().minusMinutes(30)); // Locked 30 minutes ago

        // When
        boolean result = user.isAccountNonLocked();

        // Then
        assertFalse(result); // Should remain locked
        assertEquals(5, user.getFailedLoginAttempts()); // Should not reset attempts
        assertFalse(user.isAccountNonLocked()); // Should remain locked
        assertNotNull(user.getLockTime()); // Should keep lock time
    }

    @Test
    void isAccountNonLocked_ShouldHandleNullLockTime() {
        // Given
        user.setAccountNonLocked(false);
        user.setLockTime(null);

        // When
        boolean result = user.isAccountNonLocked();

        // Then
        assertFalse(result); // Should remain locked when lock time is null
    }

    @Test
    void isCredentialsNonExpired_ShouldAlwaysReturnTrue() {
        // When & Then
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_ShouldReturnEnabledStatus() {
        // Test enabled = true
        user.setEnabled(true);
        assertTrue(user.isEnabled());

        // Test enabled = false
        user.setEnabled(false);
        assertFalse(user.isEnabled());
    }

    @Test
    void user_ShouldHaveCorrectDefaultValues() {
        // Given
        User newUser = new User();

        // Then
        assertFalse(newUser.isEnabled()); // Default is false
        assertEquals(0, newUser.getFailedLoginAttempts()); // Default is 0
        assertTrue(newUser.isAccountNonLocked()); // Default is true
        assertNull(newUser.getLockTime());
        assertNull(newUser.getLastLoginTime());
    }

    @Test
    void user_AllArgsConstructor_ShouldWork() {
        // Given
        Long id = 2L;
        String username = "newuser";
        String password = "newpassword";
        String email = "new@example.com";
        Long statusId = 1L;
        String mobileNumber = "1234567890";
        UserRole role = UserRole.ADMIN;
        LocalDateTime lastLoginTime = LocalDateTime.now();
        Date passwordExpiryDate = new Date();
        boolean enabled = true;
        int failedLoginAttempts = 2;
        boolean accountNonLocked = true;
        LocalDateTime lockTime = LocalDateTime.now();

        // When
        User user = new User(id, username, password, email, statusId, mobileNumber, 
                           role, lastLoginTime, passwordExpiryDate, enabled, 
                           failedLoginAttempts, accountNonLocked, lockTime);

        // Then
        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(email, user.getEmail());
        assertEquals(statusId, user.getStatusId());
        assertEquals(mobileNumber, user.getMobileNumber());
        assertEquals(role, user.getRole());
        assertEquals(lastLoginTime, user.getLastLoginTime());
        assertEquals(passwordExpiryDate, user.getPasswordExpiryDate());
        assertEquals(enabled, user.isEnabled());
        assertEquals(failedLoginAttempts, user.getFailedLoginAttempts());
        assertEquals(accountNonLocked, user.isAccountNonLocked());
        assertEquals(lockTime, user.getLockTime());
    }

    @Test
    void user_EqualsAndHashCode_ShouldWork() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("testuser");
        user1.setEmail("test@example.com");

        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("testuser");
        user2.setEmail("test@example.com");

        User user3 = new User();
        user3.setId(2L);
        user3.setUsername("differentuser");
        user3.setEmail("different@example.com");

        // Then
        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void user_ToString_ShouldWork() {
        // When
        String toString = user.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@example.com"));
    }

    @Test
    void lockingBehavior_IntegrationTest() {
        // Test the complete locking/unlocking behavior
        
        // Start with clean user
        User testUser = new User();
        testUser.setAccountNonLocked(true);
        testUser.setFailedLoginAttempts(0);

        // Simulate 4 failed attempts
        for (int i = 0; i < 4; i++) {
            testUser.incrementFailedAttempts();
            assertTrue(testUser.isAccountNonLocked(), "Should not be locked after " + (i + 1) + " attempts");
        }

        // 5th attempt should lock the account
        testUser.incrementFailedAttempts();
        assertFalse(testUser.isAccountNonLocked(), "Should be locked after 5 attempts");
        assertNotNull(testUser.getLockTime());

        // Manually reset (simulating admin unlock or time expiry)
        testUser.resetFailedAttempts();
        assertTrue(testUser.isAccountNonLocked(), "Should be unlocked after reset");
        assertEquals(0, testUser.getFailedLoginAttempts());
        assertNull(testUser.getLockTime());
    }
}