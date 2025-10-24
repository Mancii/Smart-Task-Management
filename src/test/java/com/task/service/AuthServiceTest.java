package com.task.service;

import com.task.constants.MainConstants;
import com.task.dto.AuthResponse;
import com.task.dto.AuthenticationRequest;
import com.task.dto.UserRegistrationDto;
import com.task.entity.User;
import com.task.entity.UserRole;
import com.task.entity.VerificationToken;
import com.task.exception.AccountLockedException;
import com.task.exception.BusinessException;
import com.task.repo.UserRepository;
import com.task.utils.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper mapper;

    @Mock
    private TokenService tokenService;

    @Mock
    private EmailService emailService;

    @Mock
    private VerificationTokenService verificationTokenService;

    @InjectMocks
    private AuthService authService;

    private AuthenticationRequest authRequest;
    private User user;
    private VerificationToken verificationToken;

    @BeforeEach
    void setUp() {
        // Set up test values using ReflectionTestUtils
        ReflectionTestUtils.setField(authService, "appBaseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(authService, "passwordExpirationDays", 90);

        authRequest = new AuthenticationRequest();
        authRequest.setUserName("testuser");
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("Password123!");
        authRequest.setMobileNumber("01012345678");
        authRequest.setRole(UserRole.USER);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setPasswordExpiryDate(DateUtil.addDaysToNow(90));
        user.setRole(UserRole.USER);

        verificationToken = new VerificationToken();
        verificationToken.setToken("test-token");
        verificationToken.setUser(user);
    }

    @Test
    void register_ShouldCreateUserSuccessfully_WhenValidRequest() throws IOException {
        // Given
        when(userRepository.existsByEmail(authRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(authRequest.getPassword())).thenReturn("encodedPassword");
        when(mapper.map(any(UserRegistrationDto.class), eq(User.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(verificationTokenService.createVerificationToken(any(User.class))).thenReturn(verificationToken);

        // When
        authService.register(authRequest);

        // Then
        verify(userRepository).existsByEmail(authRequest.getEmail());
        verify(passwordEncoder).encode(authRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(verificationTokenService).createVerificationToken(any(User.class));
        verify(emailService).sendVerificationEmail(any(User.class), anyString());
    }

    @Test
    void register_ShouldThrowBusinessException_WhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(authRequest.getEmail())).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.register(authRequest));
        
        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowBusinessException_WhenPasswordTooShort() {
        // Given
        authRequest.setPassword("short");
        when(userRepository.existsByEmail(authRequest.getEmail())).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.register(authRequest));
        
        assertEquals("Password must be at least 8 characters long", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowBusinessException_WhenEmailServiceFails() throws IOException {
        // Given
        when(userRepository.existsByEmail(authRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(authRequest.getPassword())).thenReturn("encodedPassword");
        when(mapper.map(any(UserRegistrationDto.class), eq(User.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(verificationTokenService.createVerificationToken(any(User.class))).thenReturn(verificationToken);
        doThrow(new IOException("Email service error")).when(emailService)
            .sendVerificationEmail(any(User.class), anyString());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.register(authRequest));
        
        assertEquals("Failed to send verification email. Please try again later.", exception.getMessage());
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenValidCredentials() throws Exception {
        // Given
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(userRepository.save(user)).thenReturn(user);

        // When
        AuthResponse response = authService.login(authRequest);

        // Then
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(tokenService).saveToken("access-token", "refresh-token", user.getId());
    }

    @Test
    void login_ShouldThrowBusinessException_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.login(authRequest));
        
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void login_ShouldThrowAccountLockedException_WhenAccountLocked() {
        // Given
        user.setAccountNonLocked(false);
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));

        // When & Then
        AccountLockedException exception = assertThrows(AccountLockedException.class, 
            () -> authService.login(authRequest));
        
        assertEquals("Account is locked. Please try again later or reset your password.", exception.getMessage());
    }

    @Test
    void login_ShouldThrowBusinessException_WhenPasswordIncorrect() {
        // Given
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.login(authRequest));
        
        assertEquals("Invalid email or password", exception.getMessage());
        verify(userRepository).save(user); // Should save to increment failed attempts
    }

    @Test
    void login_ShouldThrowBusinessException_WhenAccountNotEnabled() {
        // Given
        user.setEnabled(false);
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.login(authRequest));
        
        assertEquals("Please verify your email address before logging in.", exception.getMessage());
    }

    @Test
    void login_ShouldThrowBusinessException_WhenPasswordExpired() throws Exception {
        // Given
        user.setPasswordExpiryDate(new Date(System.currentTimeMillis() - 86400000)); // Yesterday
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(authRequest.getPassword(), user.getPassword())).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.login(authRequest));
        
        assertEquals("Your password has expired. Please reset your password", exception.getMessage());
    }

    @Test
    void unlockAccounts_ShouldUnlockExpiredLockedAccounts() {
        // Given
        User lockedUser1 = new User();
        lockedUser1.setId(1L);
        lockedUser1.setAccountNonLocked(false);
        lockedUser1.setFailedLoginAttempts(5);
        
        User lockedUser2 = new User();
        lockedUser2.setId(2L);
        lockedUser2.setAccountNonLocked(false);
        lockedUser2.setFailedLoginAttempts(3);

        List<User> lockedUsers = List.of(lockedUser1, lockedUser2);
        when(userRepository.findByAccountNonLockedFalseAndLockTimeBefore(any(LocalDateTime.class)))
            .thenReturn(lockedUsers);

        // When
        authService.unlockAccounts();

        // Then
        verify(userRepository).findByAccountNonLockedFalseAndLockTimeBefore(any(LocalDateTime.class));
        verify(userRepository, times(2)).save(any(User.class));
        
        // Verify that resetFailedAttempts was called on both users
        assertTrue(lockedUser1.isAccountNonLocked());
        assertEquals(0, lockedUser1.getFailedLoginAttempts());
        assertTrue(lockedUser2.isAccountNonLocked());
        assertEquals(0, lockedUser2.getFailedLoginAttempts());
    }

    @Test
    void unlockAccounts_ShouldDoNothing_WhenNoLockedAccounts() {
        // Given
        when(userRepository.findByAccountNonLockedFalseAndLockTimeBefore(any(LocalDateTime.class)))
            .thenReturn(List.of());

        // When
        authService.unlockAccounts();

        // Then
        verify(userRepository).findByAccountNonLockedFalseAndLockTimeBefore(any(LocalDateTime.class));
        verify(userRepository, never()).save(any(User.class));
    }
}