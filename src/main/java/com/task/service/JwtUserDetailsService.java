package com.task.service;

import com.task.constants.MainConstants;
import com.task.dto.ResetPasswordForm;
import com.task.entity.User;
import com.task.repo.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Calendar;

@Slf4j
@AllArgsConstructor
@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User loadUserByEmail(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
    }

    /**
     * Find user by email or username from the password form
     */
    private User findUserByIdentifiers(ResetPasswordForm passwordForm) {
        User user = findUserByEmail(passwordForm.getEmail());
        
        if (user == null) {
            user = findUserByUsername(passwordForm.getUserName());
            
            if (user != null && isEmailMismatch(user.getEmail(), passwordForm.getEmail())) {
                throw new BadCredentialsException("Invalid request");
            }
        }
        
        return user;
    }
    
    private User findUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        
        try {
            return loadUserByEmail(email);
        } catch (UsernameNotFoundException e) {
            return null;
        }
    }
    
    private User findUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        
        try {
            return (User) loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            return null;
        }
    }
    
    private boolean isEmailMismatch(String userEmail, String providedEmail) {
        return providedEmail != null && !providedEmail.isBlank() && 
               !providedEmail.equalsIgnoreCase(userEmail);
    }
    
    public void resetUserPassword(ResetPasswordForm passwordForm) {
        User jwtUser = findUserByIdentifiers(passwordForm);
        
        if (jwtUser == null) {
            throw new BadCredentialsException("Invalid request");
        }

        if (isAccountSuspended(jwtUser.getStatusId())) {
            throw new LockedException("Account is suspended. Please contact support.");
        }

        if (isAccountLocked(jwtUser.getStatusId())) {
            jwtUser.setStatusId(MainConstants.ACCOUNT_ACTIVE);
            jwtUser.resetFailedAttempts();
            log.info("Account unlocked for user: {}", jwtUser.getUsername());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 6);
        jwtUser.setPasswordExpiryDate(calendar.getTime());
        jwtUser.setPassword(passwordEncoder.encode(passwordForm.getNewPassword()));

        userRepository.save(jwtUser);
        log.info("Password reset successfully for user: {}", jwtUser.getUsername());
    }

    public boolean isAccountSuspended(long statusId) {
        return statusId == MainConstants.ACCOUNT_SUSPENDED;
    }

    public boolean isAccountLocked(long statusId) {
        return statusId == MainConstants.ACCOUNT_LOCKED;
    }

    public void updatePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 6);
        user.setPasswordExpiryDate(calendar.getTime());
        user.setPassword(passwordEncoder.encode(newPassword));

//        invalidateUserSessions(user); // logout everywhere except this session

        userRepository.save(user);
    }

    public void updateProfile(User user, String email, String mobile) {
        if (StringUtils.hasText(email)) {
            user.setEmail(email);
        }
        if (StringUtils.hasText(mobile)) {
            user.setMobileNumber(mobile);
        }
        userRepository.save(user);
    }

}

