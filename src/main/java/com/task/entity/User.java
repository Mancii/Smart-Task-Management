package com.task.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "USER_NAME", nullable = false)
    private String username;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "STATUS_ID")
    private Long statusId;

    @Column(name = "MOBILE_NUMBER")
    private String mobileNumber;

    @Column(name = "USER_ROLE")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "PASSWORD_EXPIRY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date passwordExpiryDate;

    @Column(name = "ENABLED", nullable = false)
    private boolean enabled = false;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;

    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountNonLocked = false;
            this.lockTime = LocalDateTime.now();
        }
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.accountNonLocked = true;
        this.lockTime = null;
    }

    public void updateLastLoginTime() {
        this.lastLoginTime = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Check if account is locked and if the lock has expired
        if (!accountNonLocked) {
            if (lockTime != null && lockTime.plusHours(1).isBefore(LocalDateTime.now())) {
                // Auto-unlock after 1 hour
                resetFailedAttempts();
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

