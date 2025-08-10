package com.task.service;

import com.task.constants.MainConstants;
import com.task.dto.AuthResponse;
import com.task.dto.AuthenticationRequest;
import com.task.dto.UserDto;
import com.task.entity.User;
import com.task.entity.VerificationToken;
import com.task.exception.AccountLockedException;
import com.task.exception.BusinessException;
import com.task.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.task.utils.DateUtil;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper mapper;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final VerificationTokenService verificationTokenService;
    
    @Value("${app.base-url}")
    private String appBaseUrl;

    @Value("${security.password.expiration-days:90}")
    private int passwordExpirationDays;

    @Transactional
    public void register(AuthenticationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        // Validate password strength
        if (request.getPassword().length() < 8) {
            throw new BusinessException("Password must be at least 8 characters long");
        }

        // Calculate password expiry date (90 days from now by default)
        Date passwordExpiryDate = DateUtil.addDaysToNow(passwordExpirationDays);

        var userDto = UserDto.builder()
                .username(request.getUserName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .mobileNumber(request.getMobileNumber())
                .role(request.getRole()) // Always default to USER role for new registrations
                .passwordExpiryDate(passwordExpiryDate)
                .statusId(MainConstants.ACCOUNT_LOCKED) // User is locked until email is verified
                .enabled(false) // User is disabled until email is verified
                .build();

        User userEntity = mapper.map(userDto, User.class);
        userRepository.save(userEntity);

        // Create and save verification token
        VerificationToken verificationToken = verificationTokenService.createVerificationToken(userEntity);
        
        // Send verification email
        try {
            emailService.sendVerificationEmail(userEntity, verificationToken.getToken());
        } catch (Exception e) {
            log.error("Failed to send verification email", e);
            throw new BusinessException("Failed to send verification email. Please try again later.");
        }
    }

    public AuthResponse login(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login attempt with non-existent email: {}", request.getEmail());
                    return new BusinessException("Invalid email or password");
                });

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            throw new AccountLockedException("Account is locked. Please try again later or reset your password.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.incrementFailedAttempts();
            userRepository.save(user);
            int remainingAttempts = 5 - user.getFailedLoginAttempts();
            String message = remainingAttempts > 0
                    ? String.format("Invalid credentials. %d attempts remaining.", remainingAttempts)
                    : "Account locked due to multiple failed login attempts. Please try again in 1 hour";

            throw new BusinessException(message);
        }

        // Update last login time
        user.updateLastLoginTime();

        // Reset failed attempts on successful login
        if (user.getFailedLoginAttempts() > 0) {
            user.resetFailedAttempts();
        }

        if (!user.isEnabled()) {
            throw new BusinessException("Account not verified. Please check your email and verify your account");
        }

        // Check if password has expired (only if user has logged in before and it's been more than 3 months)
        if (user.getLastLoginTime() != null &&
                user.getLastLoginTime().isBefore(LocalDateTime.now().minusMonths(3)) &&
                DateUtil.isDateBeforeNow(user.getPasswordExpiryDate())) {
            throw new BusinessException("Your password has expired. Please reset your password");
        }
        
        // Check if password has expired
        if (DateUtil.isDateBeforeNow(user.getPasswordExpiryDate())) {
            throw new BusinessException("Your password has expired. Please reset your password");
        }

        user = userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        tokenService.saveToken(jwtToken, refreshToken, user.getId());

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void unlockAccounts() {
        log.info("Running account unlock job");
        List<User> lockedUsers = userRepository.findByAccountNonLockedFalseAndLockTimeBefore(
                LocalDateTime.now().minusHours(1)
        );

        if (!lockedUsers.isEmpty()) {
            log.info("Unlocking {} accounts", lockedUsers.size());
            lockedUsers.forEach(user -> {
                user.resetFailedAttempts();
                userRepository.save(user);
            });
        }
    }

}