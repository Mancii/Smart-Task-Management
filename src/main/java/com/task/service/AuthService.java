package com.task.service;

import com.task.constants.MainConstants;
import com.task.dto.AuthResponse;
import com.task.dto.AuthenticationRequest;
import com.task.dto.UserDto;
import com.task.entity.User;
import com.task.entity.UserRole;
import com.task.entity.VerificationToken;
import com.task.exception.BusinessException;
import com.task.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public AuthResponse register(AuthenticationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        var userDto = UserDto.builder()
                .username(request.getUserName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : UserRole.USER)
                .statusId(MainConstants.ACCOUNT_ACTIVE)
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
            // In production, you might want to handle this differently
            throw new BusinessException("Failed to send verification email. Please try again later.");
        }

        // Generate tokens but user won't be able to use them until email is verified
        var jwtToken = jwtService.generateToken(userEntity);
        var refreshToken = jwtService.generateRefreshToken(userEntity);
        tokenService.saveToken(jwtToken, refreshToken, userEntity.getId());

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .message("Registration successful. Please check your email to verify your account.")
                .build();
    }

    public AuthResponse login(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new IllegalStateException("Account not verified. Please check your email and verify your account.");
        }

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        tokenService.saveToken(jwtToken, refreshToken, user.getId());

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

}