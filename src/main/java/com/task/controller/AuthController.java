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
    public ResponseEntity<ApiResponse> register(
            @RequestBody @Valid AuthenticationRequest request) {
        authService.register(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new ApiResponse(
                true,
                "Registration successful. Please check your email to verify your account.",
                null
            ));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody JwtRefreshRequest jwtRefreshRequest) {
        return ResponseEntity.ok(tokenService.
                getUserNameFromTokenUsingRefreshToken(jwtRefreshRequest.getRefreshToken())
        );
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordForm passwordForm)
            throws Exception {

        jwtUserDetailsService.resetUserPassword(passwordForm);

        return ResponseEntity.ok(new LogoutResponse("Reset Password taken Successfully"));

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader String Authorization) {
        Authorization = Authorization.substring(7);

        try {
            return ResponseEntity.ok(tokenService.logout(Authorization));
        } catch (Exception e) {
            return ResponseEntity.ok(new ErrorResponse("invalid Token", 498));
        }

    }

    @PostMapping("/kill")
    public ResponseEntity<?> killSession(@RequestParam long id) {
        try {
            return ResponseEntity.ok(tokenService.kill(id));
        } catch (Exception e) {
            return ResponseEntity.ok(new ErrorResponse("invalid Token", 498));
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        try {
            verificationTokenService.verifyEmailToken(token);
            
            return ResponseEntity.ok(new ApiResponse(
                true,
                "Email verified successfully. You can now log in.",
                null
            ));
            
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(
                    false,
                    e.getMessage(),
                    null
                ));
        } catch (Exception e) {
            log.error("Email verification failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(
                    false,
                    "An error occurred while verifying your email. Please try again.",
                    null
                ));
        }
    }

}












