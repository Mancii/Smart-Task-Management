package com.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.dto.*;
import com.task.exception.AccountLockedException;
import com.task.exception.BusinessException;
import com.task.exception.InvalidTokenException;
import com.task.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private JwtUserDetailsService jwtUserDetailsService;

    @MockBean
    private VerificationTokenService verificationTokenService;

    private AuthenticationRequest authRequest;
    private AuthResponse authResponse;
    private JwtRefreshRequest refreshRequest;
    private ResetPasswordForm resetPasswordForm;

    @BeforeEach
    void setUp() {
        authRequest = new AuthenticationRequest();
        authRequest.setUserName("testuser");
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("Password123!");
        authRequest.setMobileNumber("01012345678");

        authResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        refreshRequest = new JwtRefreshRequest("refresh-token");

        resetPasswordForm = new ResetPasswordForm();
        resetPasswordForm.setEmail("test@example.com");
        resetPasswordForm.setNewPassword("NewPassword123!");
    }

    @Test
    @WithMockUser
    void register_ShouldReturnCreated_WhenValidRequest() throws Exception {
        // Given
        doNothing().when(authService).register(any(AuthenticationRequest.class));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful. Please check your email to verify your account."))
                .andExpect(jsonPath("$.data").value(""));

        verify(authService).register(any(AuthenticationRequest.class));
    }

    @Test
    @WithMockUser
    void register_ShouldReturnBadRequest_WhenEmailAlreadyExists() throws Exception {
        // Given
        doThrow(new BusinessException("Email already exists"))
                .when(authService).register(any(AuthenticationRequest.class));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());

        verify(authService).register(any(AuthenticationRequest.class));
    }

    @Test
    @WithMockUser
    void register_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Given
        AuthenticationRequest invalidRequest = new AuthenticationRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("short");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(AuthenticationRequest.class));
    }

    @Test
    @WithMockUser
    void authenticate_ShouldReturnOk_WhenValidCredentials() throws Exception {
        // Given
        when(authService.login(any(AuthenticationRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/authenticate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Authentication successful"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));

        verify(authService).login(any(AuthenticationRequest.class));
    }

    @Test
    @WithMockUser
    void authenticate_ShouldReturnBadRequest_WhenInvalidCredentials() throws Exception {
        // Given
        when(authService.login(any(AuthenticationRequest.class)))
                .thenThrow(new BusinessException("Invalid email or password"));

        // When & Then
        mockMvc.perform(post("/api/auth/authenticate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());

        verify(authService).login(any(AuthenticationRequest.class));
    }

    @Test
    @WithMockUser
    void authenticate_ShouldReturnBadRequest_WhenAccountLocked() throws Exception {
        // Given
        when(authService.login(any(AuthenticationRequest.class)))
                .thenThrow(new AccountLockedException("Account is locked"));

        // When & Then
        mockMvc.perform(post("/api/auth/authenticate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest());

        verify(authService).login(any(AuthenticationRequest.class));
    }

    @Test
    @WithMockUser
    void refreshToken_ShouldReturnOk_WhenValidRefreshToken() throws Exception {
        // Given
        when(tokenService.getUserNameFromTokenUsingRefreshToken(anyString()))
                .thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/refreshToken")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));

        verify(tokenService).getUserNameFromTokenUsingRefreshToken("refresh-token");
    }

    @Test
    @WithMockUser
    void refreshToken_ShouldReturnBadRequest_WhenInvalidRefreshToken() throws Exception {
        // Given
        when(tokenService.getUserNameFromTokenUsingRefreshToken(anyString()))
                .thenThrow(new BusinessException("Invalid refresh token"));

        // When & Then
        mockMvc.perform(post("/api/auth/refreshToken")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isBadRequest());

        verify(tokenService).getUserNameFromTokenUsingRefreshToken("refresh-token");
    }

    @Test
    @WithMockUser
    void resetPassword_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Given
        doNothing().when(jwtUserDetailsService).resetUserPassword(any(ResetPasswordForm.class));

        // When & Then
        mockMvc.perform(post("/api/auth/resetPassword")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetPasswordForm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successful. You can now log in with your new password."));

        verify(jwtUserDetailsService).resetUserPassword(any(ResetPasswordForm.class));
    }

    @Test
    @WithMockUser
    void resetPassword_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Given
        ResetPasswordForm invalidForm = new ResetPasswordForm();
        invalidForm.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/auth/resetPassword")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidForm)))
                .andExpect(status().isBadRequest());

        verify(jwtUserDetailsService, never()).resetUserPassword(any(ResetPasswordForm.class));
    }

    @Test
    @WithMockUser
    void logout_ShouldReturnOk_WhenValidToken() throws Exception {
        // Given
        doNothing().when(tokenService).logout(anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Successfully logged out"));

        verify(tokenService).logout("valid-token");
    }

    @Test
    @WithMockUser
    void logout_ShouldReturnCustomStatus_WhenLogoutFails() throws Exception {
        // Given
        doThrow(new RuntimeException("Logout failed"))
                .when(tokenService).logout(anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                .with(csrf())
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().is(498))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("AUTH_LOGOUT_FAILED"));

        verify(tokenService).logout("invalid-token");
    }

    @Test
    @WithMockUser
    void killSession_ShouldReturnOk_WhenValidId() throws Exception {
        // Given
        doNothing().when(tokenService).kill(1L);

        // When & Then
        mockMvc.perform(post("/api/auth/kill")
                .with(csrf())
                .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Session terminated successfully"));

        verify(tokenService).kill(1L);
    }

    @Test
    @WithMockUser
    void killSession_ShouldReturnCustomStatus_WhenKillFails() throws Exception {
        // Given
        doThrow(new RuntimeException("Kill failed"))
                .when(tokenService).kill(1L);

        // When & Then
        mockMvc.perform(post("/api/auth/kill")
                .with(csrf())
                .param("id", "1"))
                .andExpect(status().is(498))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("SESSION_TERMINATION_FAILED"));

        verify(tokenService).kill(1L);
    }

    @Test
    @WithMockUser
    void verifyEmail_ShouldReturnOk_WhenValidToken() throws Exception {
        // Given
        doNothing().when(verificationTokenService).verifyEmailToken("valid-token");

        // When & Then
        mockMvc.perform(get("/api/auth/verify-email")
                .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified successfully. You can now log in."));

        verify(verificationTokenService).verifyEmailToken("valid-token");
    }

    @Test
    @WithMockUser
    void verifyEmail_ShouldReturnBadRequest_WhenInvalidToken() throws Exception {
        // Given
        doThrow(new InvalidTokenException("Invalid token"))
                .when(verificationTokenService).verifyEmailToken("invalid-token");

        // When & Then
        mockMvc.perform(get("/api/auth/verify-email")
                .param("token", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_VERIFICATION_TOKEN"));

        verify(verificationTokenService).verifyEmailToken("invalid-token");
    }

    @Test
    @WithMockUser
    void verifyEmail_ShouldReturnInternalServerError_WhenUnexpectedError() throws Exception {
        // Given
        doThrow(new RuntimeException("Unexpected error"))
                .when(verificationTokenService).verifyEmailToken("token");

        // When & Then
        mockMvc.perform(get("/api/auth/verify-email")
                .param("token", "token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VERIFICATION_FAILED"));

        verify(verificationTokenService).verifyEmailToken("token");
    }
}