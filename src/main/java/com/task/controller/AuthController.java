package com.task.controller;

import com.task.dto.*;
import com.task.exception.InvalidTokenException;
import com.task.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final VerificationTokenService verificationTokenService;

    @PostMapping("/register")
    public ResponseEntity<BaseResponse<String>> register(
            @RequestBody @Valid AuthenticationRequest request) {
        authService.register(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BaseResponse.success("Registration successful. Please check your email to verify your account.", ""));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<BaseResponse<AuthResponse>> authenticate(
            @RequestBody @Valid AuthenticationRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(BaseResponse.success("Authentication successful", authResponse));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<BaseResponse<?>> refreshToken(@RequestBody JwtRefreshRequest jwtRefreshRequest) {
        Object response = tokenService.getUserNameFromTokenUsingRefreshToken(jwtRefreshRequest.getRefreshToken());
        return ResponseEntity.ok(BaseResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<BaseResponse<String>> resetPassword(@RequestBody ResetPasswordForm passwordForm)
            throws Exception {
        jwtUserDetailsService.resetUserPassword(passwordForm);
        return ResponseEntity.ok(BaseResponse.success("Password reset successful. You can now log in with your new password.", ""));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<String>> logout(@RequestHeader String Authorization) {
        String token = Authorization.substring(7);
        try {
            tokenService.logout(token);
            return ResponseEntity.ok(BaseResponse.success("Successfully logged out", ""));
        } catch (Exception e) {
            return ResponseEntity
                .status(498)
                .body(BaseResponse.error("Logout failed", "AUTH_LOGOUT_FAILED", e.getMessage()));
        }
    }

    @PostMapping("/kill")
    public ResponseEntity<BaseResponse<String>> killSession(@RequestParam long id) {
        try {
            tokenService.kill(id);
            return ResponseEntity.ok(BaseResponse.success("Session terminated successfully", ""));
        } catch (Exception e) {
            return ResponseEntity
                .status(498)
                .body(BaseResponse.error("Failed to terminate session", "SESSION_TERMINATION_FAILED", e.getMessage()));
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<BaseResponse<String>> verifyEmail(@RequestParam String token) {
        try {
            verificationTokenService.verifyEmailToken(token);
            return ResponseEntity.ok(
                BaseResponse.success("Email verified successfully. You can now log in.", "")
            );
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(
                    e.getMessage(),
                    "INVALID_VERIFICATION_TOKEN",
                    "The verification token is invalid or has expired."
                ));
        } catch (Exception e) {
            log.error("Email verification failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(
                    "An error occurred while verifying your email. Please try again.",
                    "VERIFICATION_FAILED",
                    e.getMessage()
                ));
        }
    }

}
